package com.moretf.repository;

import com.moretf.dto.CommendSummary;
import com.moretf.model.CommendEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class LogCommendRepositoryImpl implements LogCommendRepository {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void saveCommend(CommendEvent event) {
        String sql = "INSERT INTO log_commend_events (log_id, commended_id, commender_id) " +
                "VALUES (?, ?, ?) ON CONFLICT DO NOTHING";

        jdbcTemplate.update(sql,
                event.getLogId(),
                event.getCommendedId(),
                event.getCommenderId()
        );
    }

    @Override
    public List<CommendSummary> getCommendSummaryForLog(String logId) {
        String sql = "SELECT commended_id, COUNT(*) AS commend_count " +
                "FROM log_commend_events " +
                "WHERE log_id = ? " +
                "GROUP BY commended_id " +
                "ORDER BY commend_count DESC";

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapToSummary(rs), logId);
    }

    @Override
    public List<String> getCommendedIdsByUser(String logId, String commenderId) {
        String sql = "SELECT commended_id FROM log_commend_events " +
                "WHERE log_id = ? AND commender_id = ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("commended_id"), logId, commenderId);
    }

    private CommendSummary mapToSummary(ResultSet rs) throws SQLException {
        CommendSummary summary = new CommendSummary();
        summary.setSteamId(rs.getString("commended_id"));
        summary.setCount(rs.getInt("commend_count"));
        return summary;
    }
}
