package com.moretf.parser;

import com.moretf.model.LogEvent;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;
import java.util.HashMap;
import java.util.Map;

public class ConsoleSayEventParser implements LogLineParser {
    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): \"Console<\\d+><Console><Console>\" say \"(.*?)\""
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
                .actor(new LogEvent.Actor("Console", "Console", "Console"))
                .raw(line)
                .eventType("console_say")
                .message(m.group(2))
                .build();
    }

    private long convertToEpoch(String ts) {
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
