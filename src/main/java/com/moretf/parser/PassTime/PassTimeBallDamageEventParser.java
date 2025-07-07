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

public class PassTimeBallDamageEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): passtime_ball took damage victim '(\\d+)' attacker '(\\d+)' inflictor '(\\d+)' damage '([\\d\\.]+)' damagetype '(\\d+)' inflictor classname '(.*?)'"
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
        extras.put("victim_id", m.group(2));
        extras.put("attacker_id", sanitizeSteamId(m.group(3)));
        extras.put("inflictor_id", sanitizeSteamId(m.group(4)));
        extras.put("damage", m.group(5));
        extras.put("damagetype", m.group(6));
        extras.put("inflictor_classname", m.group(7));

        return new LogEvent(
                eventId,
                convertToEpoch(m.group(1)),
                new LogEvent.Actor("passtime_ball", "ball", "neutral"),
                line,
                "pass_ball_damage",
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
