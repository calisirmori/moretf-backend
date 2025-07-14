package com.moretf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretf.model.LogEvent;
import com.moretf.parser.*;
import com.moretf.parser.PassTime.*;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class LogEventParsingManager {
    private final List<LogLineParser> parsers;

    public LogEventParsingManager() {
        this.parsers = List.of(
                new DamageEventParser(),
                new ClassChangeEventParser(),
                new KillEventParser(),
                new KillAssistEventParser(),
                new ItemPickupEventParser(),
                new HealingEventParser(),
                new SpawnEventParser(),
                new KilledObjectEventParser(),
                new PlayerExtinguishedEventParser(),
                new EmptyUberEventParser(),
                new LostUberAdvantageEventParser(),
                new SayEventParser(),
                new KilledObjectEventParser(),
                new FirstHealAfterSpawnParser(),
                new TeamScoreParser(),
                new WorldEventParser(),
                new CaptureBlockedEventParser(),
                new PointCapturedEventParser(),
                new TeamTriggerEventParser(),
                new ConsoleSayEventParser(),
                new JoinedTeamEventParser(),
                new EnteredGameEventParser(),
                new ConnectedEventParser(),
                new NameChangeEventParser(),
                new SuicideEventParser(),
                new DisconnectedEventParser(),
                new MatchPauseEventParser(),
                new FlagEventParser(),
                new ChargeEventParser(),
                new DominationRevengeEventParser(),
                new ShotEventParser(),
                new LiquidAttackEventParser(),
                new PlayerObjectEventParser(),
                new MedicDeathCombinedParser(),
                new TeamChangeParser(),

                // Passtime-specific
                new PassCaughtEventParser(),
                new PassFreeEventParser(),
                new PassGetEventParser(),
                new PassScoreEventParser(),
                new PassScoreAssistEventParser(),
                new PassTimeBallDamageEventParser(),
                new PassBallStolenEventParser(),

                // DemosTF-specific
                new DemosTfEventParser()
        );
    }

    public List<LogEvent> parseLines(List<String> lines) {
        List<LogEvent> result = new ArrayList<>();
        int eventId = 0;

        for (String line : lines) {
            if (shouldIgnoreLine(line)) continue;

            for (LogLineParser parser : parsers) {
                if (parser.matches(line)) {
                    LogEvent event = parser.parse(line, eventId++);
                    if (event != null) {
                        result.add(event);
                    }
                    break;
                }
            }
        }

        return result;
    }

    public void saveParsedEventsToZip(List<LogEvent> parsedEvents, String fileName) {
        try {
            String dir = "src/main/resources/logs";
            Files.createDirectories(Paths.get(dir));

            String jsonPath = dir + "/parsed_log_" + fileName + ".json";
            String zipPath = dir + "/parsed_log_" + fileName + ".zip";

            ObjectMapper mapper = new ObjectMapper();
            mapper.writerWithDefaultPrettyPrinter().writeValue(new File(jsonPath), parsedEvents);

            try (ZipOutputStream zipOut = new ZipOutputStream(new FileOutputStream(zipPath))) {
                zipOut.putNextEntry(new ZipEntry("parsed_log.json"));
                byte[] jsonBytes = Files.readAllBytes(Paths.get(jsonPath));
                zipOut.write(jsonBytes);
                zipOut.closeEntry();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public List<LogLineParser> getParsers() {
        return this.parsers;
    }

    public boolean shouldIgnoreLine(String line) {
        return line.contains("position_report")
                || line.contains("STEAM USERID validated")
                || line.contains("Log file closed.")
                || line.contains("Log file started")
                || line.contains("passtime_ball took damage victim")
                || line.contains("passtime_ball spawned")
                || line.contains("Demos must be at least 5")
                || line.contains("with m_filter on")
                || line.contains("Printing for client:")
                || line.contains("_catapult1\" with the jack")
                || line.contains("_catapult2\" with the jack")
                || line.contains("Panacea check - Distance from top spawner:")
                || line.contains("[SteamNetworkingSockets]");
    }

}
