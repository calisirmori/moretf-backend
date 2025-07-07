package com.moretf.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URL;

public class LogsTfApiClient {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static JsonNode fetchLogInfo(int logId) {
        try {
            URL url = new URL("https://logs.tf/api/v1/log/" + logId);
            JsonNode root = objectMapper.readTree(url);
            return root.get("info");
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch log data for log ID: " + logId, e);
        }
    }
}
