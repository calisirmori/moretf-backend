package com.moretf.parser;

import com.moretf.model.LogEvent;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.regex.*;

public class EnteredGameEventParser implements LogLineParser {
    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><.*?>\" entered the game"
    );

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        return new LogEvent(
                eventId,
                convertToEpoch(m.group(1)),
                new LogEvent.Actor(m.group(2), m.group(3), null),
                line,
                "entered_game",
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
                null
        );
    }

    private long convertToEpoch(String ts) {
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
