package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class KillEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Unassigned)>\" killed " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Unassigned)>\" with \"(.*?)\"" +
                    "(.*)" // group 9: extras
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
        String actorName = m.group(2);
        String actorSteam = m.group(3);
        String actorTeam = m.group(4);

        String targetName = m.group(5);
        String targetSteam = m.group(6);
        String targetTeam = m.group(7);

        String weapon = m.group(8);
        String extrasRaw = m.group(9);

        Map<String, Object> extras = new HashMap<>();
        String attackerPosition = null;
        String victimPosition = null;
        String customKill = null;

        Pattern kvPattern = Pattern.compile("\\(([^\\s]+)\\s+\"([^\"]*)\"\\)");
        Matcher extraMatcher = kvPattern.matcher(extrasRaw);
        while (extraMatcher.find()) {
            String key = extraMatcher.group(1);
            String value = extraMatcher.group(2);
            if ("attacker_position".equals(key)) {
                attackerPosition = value;
            } else if ("victim_position".equals(key)) {
                victimPosition = value;
            } else if ("customkill".equals(key)) {
                customKill = value;
            } else {
                extras.put(key, value);
            }
        }

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(timestampStr))
                .actor(new LogEvent.Actor(actorName, actorSteam, actorTeam))
                .target(new LogEvent.Target(targetName, targetSteam, targetTeam))
                .raw(line)
                .eventType("kill")
                .weapon(weapon)
                .attackerPosition(attackerPosition)
                .victimPosition(victimPosition)
                .customkill(customKill)
                .extras(!extras.isEmpty() ? extras : null)
                .build();
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
