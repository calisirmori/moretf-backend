package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamChangeParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Spectator|Unassigned)>\" joined team \"(Red|Blue|Spectator|Unassigned)\""
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
        String name = m.group(2);
        String steamId = m.group(3);
        String oldTeam = m.group(4);
        String newTeam = m.group(5);

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(timestampStr))
                .actor(new LogEvent.Actor(name, steamId, oldTeam))
                .eventType("team_change")
                .team(newTeam)
                .raw(line)
                .build();
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
