package com.moretf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;

import java.util.Map;

@Service
public class SecretManagerService {

    private final SecretsManagerClient client = SecretsManagerClient.builder()
            .region(Region.US_EAST_2) // change to your actual region
            .build();

    private final ObjectMapper mapper = new ObjectMapper();

    public String getSteamApiKey() {
        try {
            GetSecretValueResponse response = client.getSecretValue(
                    GetSecretValueRequest.builder()
                            .secretId("moretf/backend/db")
                            .build()
            );

            String json = response.secretString();
            Map<String, String> secretMap = mapper.readValue(json, Map.class);
            return secretMap.get("steamApiKey");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load Steam API key from AWS Secrets Manager", e);
        }
    }
}
