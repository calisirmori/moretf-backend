package com.moretf.controller;

import com.moretf.model.ApiKey;
import com.moretf.repository.ApiKeyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.crypto.bcrypt.BCrypt;

@RestController
@RequestMapping("/api-keys")
public class ApiKeyController {

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    @PostMapping
    public Map<String, String> createApiKey(@RequestBody Map<String, String> body) {
        String userId = body.get("userId");
        String description = body.getOrDefault("description", null);

        if (userId == null || userId.isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }

        // ❗ Deactivate all old active keys for this user
        apiKeyRepository.deactivateAllByUserId(userId);

        // Generate secure key
        String rawKey = generateSecureToken(32);
        String keyHash = BCrypt.hashpw(rawKey, BCrypt.gensalt());

        ApiKey apiKey = new ApiKey();
        apiKey.setKeyHash(keyHash);
        apiKey.setUserId(userId);
        apiKey.setDescription(description);
        apiKey.setCreatedAt(LocalDateTime.now());
        apiKey.setActive(true);

        apiKeyRepository.save(apiKey);

        Map<String, String> response = new HashMap<>();
        response.put("apiKey", rawKey); // Show only once!
        return response;
    }


    private String generateSecureToken(int byteLength) {
        SecureRandom secureRandom = new SecureRandom();
        byte[] token = new byte[byteLength];
        secureRandom.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token);
    }
}
