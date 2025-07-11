package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PointCapturedEventParser implements LogLineParser {

    private static final Pattern BASE_PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): Team \"(Red|Blue)\" triggered \"pointcaptured\" " +
                    "\\(cp \"(\\d+)\"\\) \\(cpname \"(.*?)\"\\) \\(numcappers \"(\\d+)\"\\)"
    );

    private static final Pattern PLAYER_PATTERN = Pattern.compile(
            "\\(player(\\d+) \"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\"\\) \\(position\\1 \"([\\d\\- ]+)\"\\)"
    );

    @Override
    public boolean matches(String line) {
        return BASE_PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher base = BASE_PATTERN.matcher(line);
        if (!base.find()) return null;

        String timestampStr = base.group(1);
        String team = base.group(2);
        String cp = base.group(3);
        String cpName = base.group(4);
        int numCappers = Integer.parseInt(base.group(5));

        Map<String, Object> extras = new HashMap<>();
        extras.put("cp", cp);
        extras.put("cpname", cpName);
        extras.put("numcappers", numCappers);

        Matcher players = PLAYER_PATTERN.matcher(line);
        List<Map<String, String>> cappers = new ArrayList<>();

        while (players.find()) {
            Map<String, String> capper = new HashMap<>();
            capper.put("name", players.group(2));
            capper.put("steamId", players.group(3));
            capper.put("team", players.group(4));
            capper.put("position", players.group(5));
            cappers.add(capper);
        }

        extras.put("cappers", cappers);

        // Derive capturing team from first capper (if available)
        if (!cappers.isEmpty()) {
            String capturingTeam = cappers.get(0).get("team");
            if (capturingTeam != null) {
                extras.put("capturingTeam", capturingTeam);
            }
        }

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(timestampStr))
                .raw(line)
                .eventType("pointcaptured")
                .extras(extras)
                .build();
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
