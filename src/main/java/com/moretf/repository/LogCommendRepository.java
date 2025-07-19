package com.moretf.repository;

import com.moretf.dto.CommendSummary;
import com.moretf.model.CommendEvent;

import java.util.List;

public interface LogCommendRepository {
    void saveCommend(CommendEvent event);
    List<CommendSummary> getCommendSummaryForLog(String logId);
    List<String> getCommendedIdsByUser(String logId, String commenderId);
}
