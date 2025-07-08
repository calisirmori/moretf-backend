package com.moretf.service;

import com.moretf.LogMetaData.LogMetaSummaryBuilder;
import com.moretf.model.LogUploadResult;
import com.moretf.model.LogEvent;
import com.moretf.LogMetaData.LogSummary;
import com.moretf.repository.LogSummaryRepository;
import com.moretf.service.InitialLogParserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.InputStream;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LogProcessingService {

    private final S3Client s3Client;
    private final InitialLogParserService logParserService;
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
        String logId = originalFilename.replaceAll("[^0-9]", "");
        if (logId.isEmpty()) {
            throw new IllegalArgumentException("Could not extract numeric log ID from file name");
        }

        long logIdLong = Long.parseLong(logId);
        String s3Key = formatS3Key(logIdLong);

        try (InputStream inputStream = logfile.getInputStream()) {
            // 1. Upload to S3
            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(BUCKET_NAME)
                            .key(s3Key)
                            .build(),
                    software.amazon.awssdk.core.sync.RequestBody.fromInputStream(inputStream, logfile.getSize())
            );
        }

        // 2. Parse log again (separate stream)
        List<LogEvent> events = logParserService.parseFromResourceZipFile(logfile.getInputStream());

        // 3. Build summary
        LogSummary summary = LogMetaSummaryBuilder.extractMeta(logIdLong, events, title, map);

        // 4. Save to DB
        logSummaryRepository.save(summary);

        return new LogUploadResult(logId);
    }


    private String formatS3Key(long id) {
        long millions = (id / 1_000_000) * 1_000_000;
        long hundredThousands = (id / 100_000) * 100_000;
        return String.format("%s/%d/%d/%d.log.zip", PREFIX, millions, hundredThousands, id);
    }
}
