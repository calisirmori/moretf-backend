package com.moretf.controller;

import com.moretf.model.ApiKey;
import com.moretf.repository.ApiKeyRepository;
import com.moretf.service.LogProcessingService;
import com.moretf.model.LogUploadResult;
import com.moretf.util.MemoryMonitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/upload")
public class LogUploadController {

    private static final Logger logger = LoggerFactory.getLogger(LogUploadController.class);

    @Autowired
    private LogProcessingService logProcessingService;

    @Autowired
    private ApiKeyRepository apiKeyRepository;

    private final BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadLog(
            @RequestParam String title,
            @RequestParam(required = false) String map,
            @RequestParam String key,
            @RequestParam MultipartFile logfile,
            @RequestParam(required = false) String uploader,
            @RequestParam(required = false) String updatelog
    ) {
        ApiKey matchedKey = apiKeyRepository.findAll().stream()
                .filter(ApiKey::isActive)
                .filter(k -> bcrypt.matches(key, k.getKeyHash()))
                .findFirst()
                .orElse(null);

        if (matchedKey == null) {
            return unauthorized("Invalid API key");
        }

        if (title.length() > 40) return bad("Title too long");
        if (map != null && map.length() > 24) return bad("Map name too long");
        if (uploader != null && uploader.length() > 40) return bad("Uploader too long");
        if (logfile.isEmpty() || logfile.getSize() > 5_000_000) return bad("Logfile is missing or too large");

        try {
            matchedKey.setLastUsed(LocalDateTime.now());
            apiKeyRepository.save(matchedKey);

            MemoryMonitor.logMemoryUsage("Before processing log");

            LogUploadResult result = logProcessingService.processLogFile(
                    title, map, key, logfile, uploader, updatelog
            );

            MemoryMonitor.logMemoryUsage("After processing log");
            System.gc();
            MemoryMonitor.logMemoryUsage("After GC and log processing");

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "log_id", result.getLogId(),
                    "url", "/" + result.getLogId()
            ));

        }catch (Exception e) {
            logger.error("Failed to process log upload", e); // Full stack trace
            return ResponseEntity.status(500).body(Map.of(
                    "success", false,
                    "error", e.getMessage() != null ? e.getMessage() : "Internal server error"
            ));
        }
    }

    private ResponseEntity<Map<String, Object>> bad(String msg) {
        return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "error", msg
        ));
    }

    private ResponseEntity<Map<String, Object>> unauthorized(String msg) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
                "success", false,
                "error", msg
        ));
    }
}
