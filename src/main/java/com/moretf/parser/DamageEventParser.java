package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DamageEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" triggered \"damage\" against " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" " +
                    ".*?\\(damage \"(\\d+)\"\\).*?\\(weapon \"(.*?)\"\\)(.*?)$"
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
        String trailing = m.group(10);

        Integer heal = extractHeal(trailing);

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(timestampStr))
                .actor(new LogEvent.Actor(actorName, actorSteam, actorTeam))
                .target(new LogEvent.Target(targetName, targetSteam, targetTeam))
                .weapon(weapon)
                .damage(damage)
                .healing(heal)
                .raw(line)
                .eventType("damage")
                .build();
    }

    private Integer extractHeal(String text) {
        Matcher matcher = EXTRA_PATTERN.matcher(text);
        while (matcher.find()) {
            if (matcher.group(1).equalsIgnoreCase("healing")) {
                return Integer.parseInt(matcher.group(2));
            }
        }
        return null;
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
