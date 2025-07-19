package com.moretf.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class LogCommendService {

    private final JdbcTemplate jdbcTemplate;

    public void addCommend(String logId, String commenderId, String commendedId) {
        try {
            jdbcTemplate.update(
                    "INSERT INTO log_commend_events (log_id, commended_id, commender_id) VALUES (?, ?, ?)",
                    logId, commendedId, commenderId
            );
        } catch (Exception e) {
            // Duplicate commend, do nothing or log
        }
    }

    public Map<String, Integer> getCommendCounts(String logId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT commended_id, COUNT(*) as count FROM log_commend_events WHERE log_id = ? GROUP BY commended_id",
                logId
        );

        Map<String, Integer> counts = new HashMap<>();
        for (Map<String, Object> row : rows) {
            counts.put((String) row.get("commended_id"), ((Number) row.get("count")).intValue());
        }
        return counts;
    }

    public Map<String, Boolean> getCommendStatus(String logId, String commenderId) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(
                "SELECT commended_id FROM log_commend_events WHERE log_id = ? AND commender_id = ?",
                logId, commenderId
        );

        Map<String, Boolean> status = new HashMap<>();
        for (Map<String, Object> row : rows) {
            status.put((String) row.get("commended_id"), true);
        }
        return status;
    }
}
