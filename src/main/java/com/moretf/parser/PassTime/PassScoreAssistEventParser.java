package com.moretf.parser.PassTime;

import com.moretf.model.LogEvent;
import com.moretf.parser.LogLineParser;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PassScoreAssistEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<(?:(Red|Blue))>\" triggered \"pass_score_assist\" " +
                    "\\(position \"(.*?)\"\\)"
    );


    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        Map<String, Object> extras = new HashMap<>();
        extras.put("position", m.group(5));

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(m.group(1)))
                .actor(new LogEvent.Actor(m.group(2), sanitizeSteamId(m.group(3)), m.group(4)))
                .raw(line)
                .eventType("pass_score_assist")
                .extras(extras)
                .build();
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
