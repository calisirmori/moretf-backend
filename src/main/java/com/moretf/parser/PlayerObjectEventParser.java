package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PlayerObjectEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" " +
                    "triggered \"(player_builtobject|player_carryobject|player_dropobject|object_detonated)\" " +
                    "\\(object \"(.*?)\"\\) \\(position \"(.*?)\"\\)"
    );

    @Override
    public boolean matches(String line) {
        return PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher m = PATTERN.matcher(line);
        if (!m.find()) return null;

        String timestamp = m.group(1);
        String actorName = m.group(2);
        String actorSteam = m.group(3);
        String actorTeam = m.group(4);
        String eventType = m.group(5);
        String objectName = m.group(6);
        String position = m.group(7);

        return new LogEvent(
                eventId,
                convertToEpoch(timestamp),
                new LogEvent.Actor(actorName, actorSteam, actorTeam),
                line,
                eventType,
                null,
                objectName,
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
                Map.of("position", position)
        );
    }

    private long convertToEpoch(String ts) {
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
