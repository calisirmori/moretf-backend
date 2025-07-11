package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemPickupEventParser implements LogLineParser {

    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" " +
                    "picked up item \"(.*?)\"" +
                    "(?: \\(healing \"(\\d+)\"\\))?"
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
        String item = m.group(5);
        String healingStr  = m.group(6);

        Integer healing = null;
        if (healingStr != null) {
            healing = Integer.parseInt(healingStr);
        }

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(m.group(1)))
                .actor(new LogEvent.Actor(actorName, actorSteam, actorTeam))
                .healing(healing)
                .raw(line)
                .eventType("item_pickup")
                .item(item)
                .build();
    }

    private long convertToEpoch(String ts) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss");
        return LocalDateTime.parse(ts, formatter).toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
