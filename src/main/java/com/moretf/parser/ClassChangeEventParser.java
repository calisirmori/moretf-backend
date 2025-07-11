package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassChangeEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|unknown)>\" changed role to \"(\\w+)\""
    );

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher matcher = PATTERN.matcher(line);
        if (!matcher.find()) return null;

        String timestampStr = matcher.group(1);
        String actorName = matcher.group(2);
        String actorSteam = matcher.group(3);
        String actorTeam = matcher.group(4);
        String className = matcher.group(5);

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(timestampStr))
                .actor(new LogEvent.Actor(actorName, actorSteam, actorTeam))
                .raw(line)
                .eventType("class_change")
                .character(className)
                .build();
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
