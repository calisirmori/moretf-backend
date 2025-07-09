package com.moretf.service;

import com.moretf.model.LogEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class LogParserService {

    private final LogEventParsingManager parsingManager;
    private final S3Client s3Client;

    private static final String BUCKET_NAME = "moretf-logs-bucket";
    private static final String PREFIX = "logs";

    public List<LogEvent> parseFromResourceZipFile(InputStream inputStream) {
        List<LogEvent> events = new ArrayList<>();

        try (ZipInputStream zis = new ZipInputStream(inputStream);
             BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8))) {

            zis.getNextEntry(); // Open the first entry inside the zip

            String line;
            int eventCounter = 1;
            while ((line = reader.readLine()) != null) {
                LogEvent event = parsingManager.parse(line, eventCounter++);
                if (event != null) {
                    events.add(event);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error parsing from input stream", e);
        }

        return events;
    }

    public List<LogEvent> parseFromS3(long logId) throws IOException {
        String s3Key = resolveS3Key(logId);
        try (InputStream inputStream = s3Client.getObject(GetObjectRequest.builder()
                .bucket(BUCKET_NAME)
                .key(s3Key)
                .build())) {
            return parseFromResourceZipFile(inputStream);
        }
    }

    private String resolveS3Key(long id) {
        long millions = (id / 1_000_000) * 1_000_000;
        long hundredThousands = (id / 100_000) * 100_000;
        return String.format("%s/%d/%d/%d.log.zip", PREFIX, millions, hundredThousands, id);
    }
}
