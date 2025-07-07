package com.moretf.parser.PassTime;

import com.moretf.model.LogEvent;
import com.moretf.parser.LogLineParser;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.*;

public class PassScoreEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<(?:(Red|Blue))>\" triggered \"pass_score\"(.*)"
    );

    private static final Pattern EXTRA_KV_PATTERN = Pattern.compile("\\((\\w+) \"([^\"]+)\"\\)");

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        String timestamp = m.group(1);
        String name = m.group(2);
        String steamId = sanitizeSteamId(m.group(3));
        String team = m.group(4);
        String extrasRaw = m.group(5);

        Map<String, Object> extras = new HashMap<>();
        Matcher kvMatcher = EXTRA_KV_PATTERN.matcher(extrasRaw);
        while (kvMatcher.find()) {
            String key = kvMatcher.group(1);
            String value = kvMatcher.group(2);

            // Try to parse as number if it looks like one
            if (value.matches("^-?\\d+(\\.\\d+)?$")) {
                if (value.contains(".")) {
                    extras.put(key, Double.parseDouble(value));
                } else {
                    extras.put(key, Integer.parseInt(value));
                }
            } else {
                extras.put(key, value);
            }
        }

        return new LogEvent(
                eventId,
                convertToEpoch(timestamp),
                new LogEvent.Actor(name, steamId, team),
                line,
                "pass_score",
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
                extras
        );
    }

    private long convertToEpoch(String ts) {
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }

    private String sanitizeSteamId(String rawId) {
        if (rawId == null) return null;
        String cleaned = rawId.trim();
        if (!cleaned.startsWith("[U:1:")) cleaned = "[U:1:" + cleaned.replaceAll("[^\\d]", "");
        if (!cleaned.endsWith("]")) cleaned += "]";
        return cleaned;
    }
}
