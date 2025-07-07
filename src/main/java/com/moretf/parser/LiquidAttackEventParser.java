package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LiquidAttackEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<([^>]+)>\" " +
                    "triggered \"(milk_attack|jarate_attack|gas_attack)\" against " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<([^>]+)>\" " +
                    "with \"([^\"]+)\" " +
                    "\\(attacker_position \"([\\d\\- ]+)\"\\) " +
                    "\\(victim_position \"([\\d\\- ]+)\"\\)"
    );

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        String timestamp = m.group(1);
        String actorName = m.group(2);
        String actorSteam = sanitizeSteamId(m.group(3));
        String actorTeam = m.group(4);
        String eventType = m.group(5);

        String targetName = m.group(6);
        String targetSteam = sanitizeSteamId(m.group(7));
        String targetTeam = m.group(8);

        String weapon = m.group(9);
        String attackerPos = m.group(10);
        String victimPos = m.group(11);

        Map<String, Object> extras = new HashMap<>();
        extras.put("weapon", weapon);
        extras.put("attacker_position", attackerPos);
        extras.put("victim_position", victimPos);

        return new LogEvent(
                eventId,
                convertToEpoch(timestamp),
                new LogEvent.Actor(actorName, actorSteam, actorTeam),
                line,
                eventType,
                new LogEvent.Target(targetName, targetSteam, targetTeam),
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
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private String sanitizeSteamId(String rawId) {
        if (rawId == null) return null;
        String cleaned = rawId.trim().replaceAll("[^\\d]", "");
        return "[U:1:" + cleaned + "]";
    }
}
