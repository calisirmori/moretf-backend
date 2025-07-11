package com.moretf.controller;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
import com.moretf.service.LogParserService;
import com.moretf.service.SummaryAggregatorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.InputStream;
import java.util.*;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/log")
@RequiredArgsConstructor
public class LogParseController {

    private final LogParserService logParserService;
    private final SummaryAggregatorService summaryAggregatorService;
    private final S3Client s3Client;

    private static final String BUCKET_NAME = "moretf-logs-bucket";
    private static final String PREFIX = "logs";

    private String resolveS3Key(long id) {
        long millions = (id / 1_000_000) * 1_000_000;
        long hundredThousands = (id / 100_000) * 100_000;
        return String.format("%s/%d/%d/%d.log.zip", PREFIX, millions, hundredThousands, id);
    }

    @GetMapping("/local-parse/{logId}")
    public List<LogEvent> parseLogFromS3(@PathVariable String logId) {
        long id;
        try {
            id = Long.parseLong(logId);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric logId: " + logId);
        }
        String s3Key = resolveS3Key(id);
        System.out.println("Attempting to load from S3 key: " + s3Key);
        try (InputStream inputStream = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET_NAME).key(s3Key).build()
        )) {
            return logParserService.parseFromResourceZipFile(inputStream);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse S3 log file: " + s3Key, e);
        }
    }

    @ResponseBody
    @GetMapping("/local-parse-full/{logId}")
    public ResponseEntity<?> parseFullMatchFromS3(@PathVariable String logId) {
        long id;
        try {
            id = Long.parseLong(logId);
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid numeric logId: " + logId));
        }

        String s3Key = resolveS3Key(id);
        try (InputStream inputStream = s3Client.getObject(
                GetObjectRequest.builder().bucket(BUCKET_NAME).key(s3Key).build()
        )) {
            List<LogEvent> events = logParserService.parseFromResourceZipFile(inputStream);
            MatchJsonResult result = summaryAggregatorService.buildMatchJson(events, (int) id, null, null);
            return ResponseEntity.ok(result);
        } catch (NoSuchKeyException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Log file not found in S3", "key", s3Key));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to parse S3 log file", "message", e.getMessage()));
        }
    }


    @GetMapping("/test/{logId}")
    public List<LogEvent> parse100RandomFromS3(@PathVariable String logId) {
        // Optional: List objects from S3 under the relevant prefix, sample randomly
        // For now, just parse the provided logId to match structure
        return parseLogFromS3(logId);
    }
}
