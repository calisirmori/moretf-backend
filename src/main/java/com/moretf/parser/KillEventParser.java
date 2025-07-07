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
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " + // timestamp
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Unassigned)>\" killed " + // actor
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Unassigned)>\" with \"(.*?)\"" + // target & weapon
                    "(?: \\(attacker_position \"(.*?)\"\\))?" + // optional attacker_position
                    "(?: \\(victim_position \"(.*?)\"\\))?" // optional victim_position
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
        String attackerPos = m.group(9);
        String victimPos = m.group(10);

        Map<String, Object> extras = new HashMap<>();
        if (attackerPos != null) extras.put("attacker_position", attackerPos);
        if (victimPos != null) extras.put("victim_position", victimPos);

        return new LogEvent(
                eventId,
                convertToEpoch(timestampStr),
                new LogEvent.Actor(actorName, actorSteam, actorTeam),
                line,
                "kill",
                new LogEvent.Target(targetName, targetSteam, targetTeam),
                weapon,
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
