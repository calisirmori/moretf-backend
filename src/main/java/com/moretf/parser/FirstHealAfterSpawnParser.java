package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FirstHealAfterSpawnParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" triggered \"first_heal_after_spawn\" " +
                    "\\(time \"([\\d.]+)\"\\)"
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
        String actorName = m.group(2);
        String actorSteam = m.group(3);
        String actorTeam = m.group(4);
        String timeStr = m.group(5);

        return new LogEvent(
                eventId,
                convertToEpoch(timestampStr),
                new LogEvent.Actor(actorName, actorSteam, actorTeam),
                line,
                "first_heal_after_spawn",
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
                Map.of("time", timeStr)  // extras
        );
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
