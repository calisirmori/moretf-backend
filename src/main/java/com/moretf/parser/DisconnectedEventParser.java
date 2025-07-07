package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DisconnectedEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<([^>]*)>\" disconnected \\(reason \"(.*?)\"\\)"
    );

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        String team = m.group(4);
        if (team == null || team.isEmpty()) team = "Unassigned"; // fallback for malformed logs

        return new LogEvent(
                eventId,
                convertToEpoch(m.group(1)),
                new LogEvent.Actor(m.group(2), m.group(3), team),
                line,
                "disconnected",
                null,
                null,
                null,
                null,
                null,
                m.group(5),
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

