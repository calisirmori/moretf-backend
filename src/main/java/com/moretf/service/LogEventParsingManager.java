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
import java.util.List;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class LogEventParsingManager {
    private final List<LogLineParser> parsers;
    private final List<LogEvent> parsedEvents = new ArrayList<>();
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

                // --- Passtime-specific parsers ---
                new PassCaughtEventParser(),
                new PassFreeEventParser(),
                new PassGetEventParser(),
                new PassScoreEventParser(),
                new PassScoreAssistEventParser(),
                new PassTimeBallDamageEventParser(),
                new PassBallStolenEventParser(),

                // --- DemosTF-specific parser ---
                new DemosTfEventParser()

        );
    }

    public LogEvent parse(String line, int eventId) {
        if (line.contains("position_report")
                || line.contains("STEAM USERID validated")
                || line.contains("Log file closed.")
                || line.contains("Log file started")
                || line.contains("passtime_ball took damage victim")
                || line.contains("passtime_ball spawned")
                || line.contains("Demos must be at least 5")
                || line.contains("with m_filter on")
                || line.contains("Printing for client:")
                || line.contains("_catapult1\" with the jack")
                || line.contains("Panacea check - Distance from top spawner:")
                || line.contains("[SteamNetworkingSockets]")
                || line.contains("_catapult2\" with the jack")

        ) {
            return null;
        }

        for (LogLineParser parser : parsers) {
            if (parser.matches(line)) {
                LogEvent parsed = parser.parse(line, eventId);
                if (parsed != null) {
                    parsedEvents.add(parsed);
                }
                return parsed;
            }
        }
        System.out.println("[Unparsed] Event #" + eventId + ": " + line);
        return null;
    }

    public List<LogEvent> getParsedEvents() {
        return parsedEvents;
    }

    public void saveParsedEventsToResources(String fileName) {
        try {
            String dir = "src/main/resources/logs";
            Files.createDirectories(Paths.get(dir));

            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
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
}
