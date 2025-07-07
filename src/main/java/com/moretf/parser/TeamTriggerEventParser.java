package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TeamTriggerEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): Team \"(Red|Blue|RED|BLUE)\" triggered \"(.*?)\""
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
        String team = m.group(2);
        String triggered = m.group(3);

        return new LogEvent(
                eventId,
                convertToEpoch(timestampStr),
                null,
                line,
                triggered.toLowerCase(),  // e.g. intermission_win_limit
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
                Map.of("team", team)
        );
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
