package com.moretf.parser;

import com.moretf.model.LogEvent;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MedicDeathCombinedParser implements LogLineParser {

    private static final Pattern DEATH_PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Unassigned)>\" triggered \"medic_death\" against " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Unassigned)>\" " +
                    "\\(healing \"(\\d+)\"\\) \\(ubercharge \"(\\d+)\"\\)"
    );

    private static final Pattern EX_PATTERN = Pattern.compile(
            "L (\\d+/\\d+/\\d+ - \\d+:\\d+:\\d+): " +
                    "\"(.+?)<\\d+><(\\[U:1:\\d+\\])><(Red|Blue|Unassigned)>\" triggered \"medic_death_ex\" " +
                    "\\(uberpct \"(\\d+)\"\\)"
    );

    @Override
    public boolean matches(String line) {
        return DEATH_PATTERN.matcher(line).find() || EX_PATTERN.matcher(line).find();
    }

    @Override
    public LogEvent parse(String line, int eventId) {
        Matcher deathMatcher = DEATH_PATTERN.matcher(line);
        if (deathMatcher.find()) {
            return parseMedicDeath(deathMatcher, line, eventId);
        }

        Matcher exMatcher = EX_PATTERN.matcher(line);
        if (exMatcher.find()) {
            return parseMedicDeathEx(exMatcher, line, eventId);
        }

        return null;
    }

    private LogEvent parseMedicDeath(Matcher m, String line, int eventId) {
        return new LogEvent(
                eventId,
                convertToEpoch(m.group(1)),
                new LogEvent.Actor(m.group(2), m.group(3), m.group(4)),
                line,
                "medic_death",
                new LogEvent.Target(m.group(5), m.group(6), m.group(7)),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Integer.parseInt(m.group(8)),
                null,
                null,
                m.group(9),
                null,
                null,
                null,
                null
        );
    }

    private LogEvent parseMedicDeathEx(Matcher m, String line, int eventId) {
        return new LogEvent(
                eventId,
                convertToEpoch(m.group(1)),
                new LogEvent.Actor(m.group(2), m.group(3), m.group(4)),
                line,
                "medic_death_ex",
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
                Map.of("uberpct", m.group(5))
        );
    }

    private long convertToEpoch(String ts) {
        return LocalDateTime.parse(ts, DateTimeFormatter.ofPattern("MM/dd/yyyy - HH:mm:ss"))
                .toInstant(ZoneOffset.UTC).toEpochMilli();
    }
}
