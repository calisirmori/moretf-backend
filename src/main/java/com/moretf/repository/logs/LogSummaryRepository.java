package com.moretf.repository.logs;

import com.moretf.LogMetaData.LogSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LogSummaryRepository extends JpaRepository<LogSummary, Long> {
    Optional<LogSummary> findById(Long logId);
}
