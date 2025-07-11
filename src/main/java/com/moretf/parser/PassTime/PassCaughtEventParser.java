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

public class PassCaughtEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<(?:(Red|Blue|Unassigned))>\" " +
                    "triggered \"pass_pass_caught\" against " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\]?)>?<(?:(Red|Blue|Unassigned))>\" " +
                    "\\(interception \"(\\d+)\"\\) " +
                    "\\(save \"(\\d+)\"\\) " +
                    "\\(handoff \"(\\d+)\"\\) " +
                    "\\(dist \"([\\d\\.]+)\"\\) " +
                    "\\(duration \"([\\d\\.]+)\"\\) " +
                    "\\(thrower_position \"([\\d\\- ]+)\"\\) " +
                    "\\(catcher_position \"([\\d\\- ]+)\"\\)"
    );


    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        // Thrower
        LogEvent.Actor actor = new LogEvent.Actor(m.group(2), sanitizeSteamId(m.group(3)), m.group(4));

        // Catcher
        LogEvent.Target target = new LogEvent.Target(m.group(5), sanitizeSteamId(m.group(6)), m.group(7));

        // Extras
        Map<String, Object> extras = new HashMap<>();
        extras.put("interception", m.group(8));
        extras.put("save", m.group(9));
        extras.put("handoff", m.group(10));
        extras.put("dist", m.group(11));
        extras.put("duration", m.group(12));
        extras.put("thrower_position", m.group(13));
        extras.put("catcher_position", m.group(14));

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(m.group(1)))
                .actor(actor)
                .target(target)
                .raw(line)
                .eventType("pass_pass_caught")
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
