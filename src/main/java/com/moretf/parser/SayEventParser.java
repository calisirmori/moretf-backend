package com.moretf.parser;

import com.moretf.model.LogEvent;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.*;

public class SayEventParser implements LogLineParser {
    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Spectator|Unassigned)>\" (say|say_team) \"(.*?)\""
    );

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(m.group(1)))
                .actor(new LogEvent.Actor(m.group(2), m.group(3), m.group(4)))
                .raw(line)
                .eventType(m.group(5))
                .message(m.group(6))
                .build();
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
