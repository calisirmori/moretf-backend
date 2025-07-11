package com.moretf.repository;

import com.moretf.LogMetaData.LogSummary;

public interface LogSummaryProcedureRepository {
    void insertLogViaProcedure(LogSummary summary);
}
