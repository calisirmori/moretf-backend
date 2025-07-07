package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DemosTfEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): \\[demos\\.tf\\]: STV available at: (https://demos\\.tf/\\d+)"
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
                null,
                line,
                "stv_available",
                null,
                null,
                null,
                null,
                m.group(2),
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
