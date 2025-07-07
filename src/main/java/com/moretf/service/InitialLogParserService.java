package com.moretf.service;

import com.moretf.model.LogEvent;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipInputStream;

@Service
public class InitialLogParserService {

    private final LogEventParsingManager parsingManager;

    public InitialLogParserService(LogEventParsingManager parsingManager) {
        this.parsingManager = parsingManager;
    }

    public List<LogEvent> parseFromResourceZipFile(File file) {
        List<LogEvent> events = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             ZipInputStream zis = new ZipInputStream(fis)) {

            zis.getNextEntry();
            BufferedReader reader = new BufferedReader(new InputStreamReader(zis, StandardCharsets.UTF_8));

            String line;
            int eventCounter = 1;
            while ((line = reader.readLine()) != null) {
                LogEvent event = parsingManager.parse(line, eventCounter++);
                if (event != null) {
                    events.add(event);
                }
            }

        } catch (IOException e) {
            throw new RuntimeException("Error parsing file: " + file.getName(), e);
        }
        return events;
    }
}
