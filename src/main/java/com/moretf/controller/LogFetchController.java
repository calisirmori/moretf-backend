package com.moretf.controller;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
import com.moretf.repository.LogSummaryRepository;
import com.moretf.service.DynamoDbService;
import com.moretf.service.LogParserService;
import com.moretf.service.SummaryAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @GetMapping("/{logId}")
    public ResponseEntity<?> getLogById(@PathVariable long logId) {
        // Step 1: Check cache
        MatchJsonResult cached = dynamoDbService.getCachedMatchJson(logId);
        if (cached != null) {
            return ResponseEntity.ok(cached);
        }

        // Step 2: Parse from S3
        try {
            // Step 2: Get title/map from RDS if available
            String title = null;
            String map = null;
            var optionalSummary = logSummaryRepository.findById(logId);
            if (optionalSummary.isPresent()) {
                title = optionalSummary.get().getTitle();
                map = optionalSummary.get().getMap();
            }

            // Step 3: Parse from S3
            List<LogEvent> tempEvents = new ArrayList<>();
            logParserService.streamFromS3(logId, tempEvents::add);

            // Step 4: Build cached version save and serve
            MatchJsonResult cacheCopy = summaryAggregatorService.buildMatchJsonWithoutEvents(tempEvents, (int) logId, title, map);

            // Step 5: Save to Dynamo
            dynamoDbService.saveEphemeralMatchJson(logId, cacheCopy);

            return ResponseEntity.ok(cacheCopy);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to process logId " + logId, "details", e.getMessage()));
        }
    }
}

