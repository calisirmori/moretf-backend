package com.moretf.controller;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
import com.moretf.repository.logs.LogSummaryRepository;
import com.moretf.service.DynamoDbService;
import com.moretf.service.LogCommendService;
import com.moretf.service.LogParserService;
import com.moretf.service.SummaryAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class LogFetchController {

    private final DynamoDbService dynamoDbService;
    private final LogParserService logParserService; // Your logic to parse from S3
    private final SummaryAggregatorService summaryAggregatorService;
    private final LogSummaryRepository logSummaryRepository;
    private final LogCommendService logCommendService;

    @GetMapping("/{logId}")
    public ResponseEntity<?> getLogById(@PathVariable long logId, Principal principal) {
        // Step 1: Check cache
        MatchJsonResult cached = dynamoDbService.getCachedMatchJson(logId);
        if (cached != null) {
            // Attach commend data before returning
            Map<String, Integer> commendCounts = logCommendService.getCommendCounts(String.valueOf(logId));
            Map<String, Boolean> userStatus = principal != null
                    ? logCommendService.getCommendStatus(String.valueOf(logId), principal.getName())
                    : Map.of();

            Map<String, Object> response = Map.of(
                    "data", cached,
                    "commendCounts", commendCounts,
                    "commendStatus", userStatus
            );
            return ResponseEntity.ok(response);
        }

        try {
            // Load title/map from RDS
            String title = null;
            String map = null;
            var optionalSummary = logSummaryRepository.findById(logId);
            if (optionalSummary.isPresent()) {
                title = optionalSummary.get().getTitle();
                map = optionalSummary.get().getMap();
            }

            // Parse S3 log events
            List<LogEvent> tempEvents = new ArrayList<>();
            logParserService.streamFromS3(logId, tempEvents::add);

            // Build summary
            MatchJsonResult cacheCopy = summaryAggregatorService.buildMatchJsonWithoutEvents(tempEvents, (int) logId, title, map);

            // Save to Dynamo
            dynamoDbService.saveEphemeralMatchJson(logId, cacheCopy);

            // Enrich with commend data
            Map<String, Integer> commendCounts = logCommendService.getCommendCounts(String.valueOf(logId));
            Map<String, Boolean> userStatus = principal != null
                    ? logCommendService.getCommendStatus(String.valueOf(logId), principal.getName())
                    : Map.of();

            Map<String, Object> response = Map.of(
                    "data", cacheCopy,
                    "commendCounts", commendCounts,
                    "commendStatus", userStatus
            );
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process logId " + logId, "details", e.getMessage()));
        }
    }

}

