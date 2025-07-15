package com.moretf.service;

import com.moretf.LogMetaData.LogMetaSummaryBuilder;
import com.moretf.model.LogUploadResult;
import com.moretf.model.LogEvent;
import com.moretf.LogMetaData.LogSummary;
import com.moretf.model.PlayerSummaryEntity;
import com.moretf.repository.logs.LogSummaryProcedureRepository;
import com.moretf.repository.logs.LogSummaryRepository;
import com.moretf.repository.players.PlayerSummaryRepository;
import com.moretf.util.MemoryMonitor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogProcessingService {

    @Autowired
    private LogSummaryProcedureRepository logSummaryProcedureRepository;

    @Autowired
    private PlayerSummaryRepository playerSummaryRepository;

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
        //uploadLogToS3(logfile, s3Key);

        MemoryMonitor.logMemoryUsage("Before Step 2");
        // Step 2: Parse and save summary to DB (and get events)
        List<LogEvent> events = saveLogSummaryToDatabase(logfile, logIdLong, title, map);
        MemoryMonitor.logMemoryUsage("After Step 2");

        // Step 3: Store a stripped version in DynamoDB cache
        //MatchJsonResult cachedJson = summaryAggregatorService.buildMatchJsonWithoutEvents(events, (int) logIdLong, title, map);
        //dynamoDbService.saveEphemeralMatchJson(logIdLong, cachedJson);
        MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
        List<GarbageCollectorMXBean> gcBeans = ManagementFactory.getGarbageCollectorMXBeans();

        long totalCollections = gcBeans.stream().mapToLong(GarbageCollectorMXBean::getCollectionCount).sum();
        MemoryMonitor.logMemoryUsage("Before Step 4");
        // Step 4: Insert player rows into `players` table
        List<PlayerSummaryEntity> playerSummaries = summaryAggregatorService.buildPlayerSummaries(events, (int) logIdLong);
        MemoryMonitor.logMemoryUsage("After Step 4");

        System.out.println("[MEMORY] GC Count: " + totalCollections);
        System.out.println("[MEMORY] Used Heap: " + memoryMXBean.getHeapMemoryUsage().getUsed() / (1024 * 1024) + "MB");

        playerSummaryRepository.bulkInsert(playerSummaries);

        playerSummaries.clear();
        playerSummaries = null;

        events.clear();
        events = null;

        System.gc();

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
        List<LogEvent> events = new ArrayList<>();
        logParserService.streamFromResourceZipFile(
                logfile.getInputStream(),
                events::add
        );
        LogSummary summary = LogMetaSummaryBuilder.extractMeta(logId, events, title, map);
        logSummaryProcedureRepository.insertLogViaProcedure(summary);
        return events;
    }

    private String formatS3Key(long id) {
        long millions = (id / 1_000_000) * 1_000_000;
        long hundredThousands = (id / 100_000) * 100_000;
        return String.format("%s/%d/%d/%d.log.zip", PREFIX, millions, hundredThousands, id);
    }
}

