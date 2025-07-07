package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WorldEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): World triggered " +
                    "\"(Round_Overtime" +
                    "|Round_Win" +
                    "|Round_Start" +
                    "|Round_Length" +
                    "|Game_Over" +
                    "|Intermission_Win_Limit" +
                    "|Round_Setup_Begin" +
                    "|Round_Setup_End" +
                    "|Round_Stalemate" +
                    "|Game_Paused" +
                    "|Game_Unpaused" +
                    "|Mini_Round_Selected" +
                    "|Mini_Round_Start" +
                    "|Mini_Round_Win" +
                    "|Mini_Round_Length" +
                    "|Pause_Length)\"" +                            // <–– added here
                    "(?: \\((.*?)\\))?(?: reason \"(.*?)\")?"
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
        String eventType = m.group(2);
        String details = m.group(3); // optional key-value details
        String reason = m.group(4); // only for Game_Over

        Map<String, Object> extras = new HashMap<>();
        if (details != null) {
            Matcher kv = Pattern.compile("(\\w+) \"([^\"]+)\"").matcher(details);
            while (kv.find()) {
                extras.put(kv.group(1), kv.group(2));
            }
        }
        if (reason != null) {
            extras.put("reason", reason);
        }

        return new LogEvent(
                eventId,
                convertToEpoch(timestampStr),
                null, // No actor
                line,
                eventType.toLowerCase(), // "round_overtime", "round_win", "round_length"
                null,
                null,
                null,
                null,
                null,
                reason,
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
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
