package com.moretf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretf.model.MatchJsonResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import software.amazon.awssdk.services.dynamodb.model.GetItemResponse;
import software.amazon.awssdk.services.dynamodb.model.PutItemRequest;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DynamoDbService {

    private final DynamoDbClient dynamoDbClient;
    private final ObjectMapper objectMapper;

    private static final String TABLE_NAME = "logs-cache";

    public void saveEphemeralMatchJson(long logId, MatchJsonResult matchJson) {
        try {
            String json = objectMapper.writeValueAsString(matchJson);
            long ttl = Instant.now().plusSeconds(30L * 24 * 60 * 60).getEpochSecond(); // 30 days from now

            Map<String, AttributeValue> item = new HashMap<>();
            item.put("logId", AttributeValue.fromN(Long.toString(logId)));
            item.put("json", AttributeValue.fromS(json));
            item.put("ttl", AttributeValue.fromN(Long.toString(ttl)));

            PutItemRequest request = PutItemRequest.builder()
                    .tableName(TABLE_NAME)
                    .item(item)
                    .build();

            dynamoDbClient.putItem(request);

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save matchJson to DynamoDB logs-cache", e);
        }
    }

    public MatchJsonResult getCachedMatchJson(long logId) {
        try {
            Map<String, AttributeValue> key = Map.of(
                    "logId", AttributeValue.fromN(Long.toString(logId))
            );

            GetItemResponse response = dynamoDbClient.getItem(builder -> builder
                    .tableName(TABLE_NAME)
                    .key(key)
            );

            if (!response.hasItem()) return null;

            String json = response.item().get("json").s();
            return objectMapper.readValue(json, MatchJsonResult.class);

        } catch (Exception e) {
            System.err.println("Failed to get cached matchJson: " + e.getMessage());
            return null;
        }
    }

}
