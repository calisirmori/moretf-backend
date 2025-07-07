package com.moretf.parser;

import com.moretf.model.LogEvent;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FlagEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<([^>]+)>\" " +
                    "triggered \"flagevent\"(.*)"
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

        LogEvent.Actor actor = new LogEvent.Actor(
                m.group(2),
                sanitizeSteamId(m.group(3)),
                m.group(4)
        );

        String extrasRaw = m.group(5);
        Map<String, Object> extras = new HashMap<>();

        Matcher kvMatcher = EXTRA_KV_PATTERN.matcher(extrasRaw);
        while (kvMatcher.find()) {
            String key = kvMatcher.group(1);
            String value = kvMatcher.group(2);
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
                convertToEpoch(m.group(1)),
                actor,
                line,
                "flagevent",
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
