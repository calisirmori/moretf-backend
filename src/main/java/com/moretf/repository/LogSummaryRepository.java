package com.moretf.repository;

import com.moretf.LogMetaData.LogSummary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LogSummaryRepository extends JpaRepository<LogSummary, Long> {
}
