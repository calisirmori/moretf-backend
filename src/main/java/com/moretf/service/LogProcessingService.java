package com.moretf.service;

import com.moretf.LogMetaData.LogMetaSummaryBuilder;
import com.moretf.model.LogUploadResult;
import com.moretf.model.LogEvent;
import com.moretf.LogMetaData.LogSummary;
import com.moretf.model.MatchJsonResult;
import com.moretf.repository.LogSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogProcessingService {

    @Autowired
    private SummaryAggregatorService summaryAggregatorService;

    @Autowired
    private DynamoDbService dynamoDbService;

    private final S3Client s3Client;
    private final LogParserService logParserService;
    private final LogSummaryRepository logSummaryRepository;

    private static final String BUCKET_NAME = "moretf-logs-bucket";
    private static final String PREFIX = "logs";

    public LogUploadResult processLogFile(
            String title,
            String map,
            String key,
            MultipartFile logfile,
            String uploader,
            String updatelog
    ) throws Exception {

        String originalFilename = logfile.getOriginalFilename();
        String logId = extractLogId(originalFilename);
        long logIdLong = Long.parseLong(logId);
        String s3Key = formatS3Key(logIdLong);

        // Step 1: Upload to S3
        uploadLogToS3(logfile, s3Key);

        // Step 2: Parse and save summary to DB (and get events)
        List<LogEvent> events = saveLogSummaryToDatabase(logfile, logIdLong, title, map);

        // Step 3: Store a stripped version in DynamoDB cache
        MatchJsonResult cachedJson = summaryAggregatorService.buildMatchJsonWithoutEvents(events, (int) logIdLong, title, map);
        dynamoDbService.saveEphemeralMatchJson(logIdLong, cachedJson);

        return new LogUploadResult(logId);
    }

    private String extractLogId(String filename) {
        if (filename == null) {
            throw new IllegalArgumentException("Filename is null");
        }

        String logId = filename.replaceAll("[^0-9]", "");
        if (logId.isEmpty()) {
            throw new IllegalArgumentException("Could not extract numeric log ID from file name");
        }

        return logId;
    }

    private void uploadLogToS3(MultipartFile logfile, String s3Key) throws Exception {
        try (InputStream inputStream = logfile.getInputStream()) {
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(s3Key)
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, logfile.getSize())
            );
        }
    }

    private List<LogEvent> saveLogSummaryToDatabase(MultipartFile logfile, long logId, String title, String map) throws Exception {
        List<LogEvent> events = logParserService.parseFromResourceZipFile(logfile.getInputStream());
        LogSummary summary = LogMetaSummaryBuilder.extractMeta(logId, events, title, map);
        logSummaryRepository.save(summary);
        return events;
    }

    private String formatS3Key(long id) {
        long millions = (id / 1_000_000) * 1_000_000;
        long hundredThousands = (id / 100_000) * 100_000;
        return String.format("%s/%d/%d/%d.log.zip", PREFIX, millions, hundredThousands, id);
    }
}

