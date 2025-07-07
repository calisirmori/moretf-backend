package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DamageEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" triggered \"damage\" against " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" " +
                    ".*?\\(damage \"(\\d+)\"\\).*?\\(weapon \"(.*?)\"\\).*"
    );

    private static final Pattern EXTRA_PATTERN = Pattern.compile("\\((\\w+)\\s+\"([^\"]+)\"\\)");

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        String timestampStr = m.group(1);
        String actorName = m.group(2);
        String actorSteam = m.group(3);
        String actorTeam = m.group(4);

        String targetName = m.group(5);
        String targetSteam = m.group(6);
        String targetTeam = m.group(7);

        int damage = Integer.parseInt(m.group(8));
        String weapon = m.group(9);

        Map<String, Object> extras = extractExtras(line);

        // Remove the base fields from extras to avoid duplication
        extras.remove("damage");
        extras.remove("weapon");

        return new LogEvent(
                eventId,
                convertToEpoch(timestampStr),
                new LogEvent.Actor(actorName, actorSteam, actorTeam),
                line,
                "damage",
                new LogEvent.Target(targetName, targetSteam, targetTeam),
                weapon,
                damage,
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
                !extras.isEmpty() ? extras : null
        );
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private Map<String, Object> extractExtras(String line) {
        Map<String, Object> extras = new HashMap<>();
        Matcher matcher = EXTRA_PATTERN.matcher(line);
        while (matcher.find()) {
            extras.put(matcher.group(1), matcher.group(2));
        }
        return extras;
    }
}
