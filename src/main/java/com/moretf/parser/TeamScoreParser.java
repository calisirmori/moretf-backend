package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamScoreParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): Team \"(Red|Blue|RED|BLUE)\" (current|final) score \"(\\d+)\" with \"(\\d+)\" players"
    );

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        String timestampStr = m.group(1);
        String team = m.group(2);
        String scoreType = m.group(3); // current or final
        int score = Integer.parseInt(m.group(4));
        int players = Integer.parseInt(m.group(5));

        Map<String, Object> extras = new HashMap<>();
        extras.put("team", team);
        extras.put("score", score);
        extras.put("players", players);

        return new LogEvent(
                eventId,
                convertToEpoch(timestampStr),
                null,
                line,
                "team_" + scoreType + "_score",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                extras
        );
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
