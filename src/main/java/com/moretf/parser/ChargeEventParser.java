package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ChargeEventParser implements LogLineParser {
    private static final Pattern PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue)>\" " +
                    "triggered \"(chargedeployed|chargeended|chargeready)\"" +
                    "(?: \\((medigun|duration) \\\"(.*?)\\\"\\))?"
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
        String eventType = m.group(5); // chargedeployed, chargeended, chargeready
        String extraKey = m.group(6);  // medigun or duration
        String extraValue = m.group(7);

        Map<String, Object> extras = (extraKey != null && extraValue != null)
                ? Map.of(extraKey, extraValue)
                : null;

        return LogEvent.builder()
                .eventId(eventId)
                .timestamp(convertToEpoch(timestampStr))
                .actor(new LogEvent.Actor(actorName, actorSteam, actorTeam))
                .raw(line)
                .eventType(eventType.toLowerCase())
                .weapon((extraKey != null && extraKey.equals("medigun")) ? extraValue : null)
                .extras(extras)
                .build();
    }

    private long convertToEpoch(String ts) {
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
