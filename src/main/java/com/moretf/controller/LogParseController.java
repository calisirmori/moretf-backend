// com/moretf/controller/LogParseController.java

package com.moretf.controller;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
import com.moretf.service.InitialLogParserService;
import com.moretf.service.SummaryAggregatorService;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.*;

@RestController
@RequestMapping("/api/log")
public class LogParseController {

    private final InitialLogParserService logParserService;

    private final SummaryAggregatorService summaryAggregatorService;

    public LogParseController(InitialLogParserService logParserService,
                              SummaryAggregatorService summaryAggregatorService) {
        this.logParserService = logParserService;
        this.summaryAggregatorService = summaryAggregatorService;
    }


    @GetMapping("/test/{logId}")
    public List<LogEvent> parse100RandomLogs(@PathVariable String logId) {
        if (logId.length() < 4) throw new IllegalArgumentException("Invalid logId prefix");

        String folderPrefix = logId.substring(0, logId.length() - 3) + "000"; // e.g., "3700001" -> "3700000"
        File dir = new File("D:/logstf/logs/" + folderPrefix);
        if (!dir.exists() || !dir.isDirectory()) {
            throw new RuntimeException("Directory does not exist: " + dir.getPath());
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".log.zip"));
        if (files == null || files.length == 0) {
            throw new RuntimeException("No log files found in: " + dir.getPath());
        }

        List<LogEvent> allEvents = new ArrayList<>();
        Random random = new Random();

        int limit = Math.min(1000, files.length); // avoid IndexOutOfBounds
        for (int i = 0; i < limit; i++) {
            File randomFile = files[random.nextInt(files.length)];
            System.out.println("Parsing: " + randomFile.getName());

            try {
                List<LogEvent> events = logParserService.parseFromResourceZipFile(randomFile);
                allEvents.addAll(events);
            } catch (Exception e) {
                System.err.println("Failed to parse: " + randomFile.getName());
                e.printStackTrace();
            }
        }

        return allEvents;
    }

    @GetMapping("/local-parse/{logId}")
    public List<LogEvent> parseLocalLogById(@PathVariable String logId) {
        int id;
        try {
            id = Integer.parseInt(logId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric logId: " + logId);
        }

        int folderBase = (id / 100000) * 100000;
        File targetFile = new File("D:/logstf/logs/" + folderBase + "/" + id + ".log.zip");

        if (!targetFile.exists() || !targetFile.isFile()) {
            throw new RuntimeException("Log file not found: " + targetFile.getPath());
        }

        List<LogEvent> parsed = logParserService.parseFromResourceZipFile(targetFile);
        return parsed;
    }

    @ResponseBody
    @GetMapping("/local-parse-full/{logId}")
    public MatchJsonResult parseFullMatch(@PathVariable String logId) {
        int id;
        try {
            id = Integer.parseInt(logId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric logId: " + logId);
        }

        int folderBase = (id / 100000) * 100000;
        File targetFile = new File("D:/logstf/logs/" + folderBase + "/" + id + ".log.zip");

        if (!targetFile.exists() || !targetFile.isFile()) {
            throw new RuntimeException("Log file not found: " + targetFile.getPath());
        }

        List<LogEvent> events = logParserService.parseFromResourceZipFile(targetFile);
        return summaryAggregatorService.buildMatchJson(events, id);
    }

}
