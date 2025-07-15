package com.moretf.repository.logs;

import com.moretf.LogMetaData.LogSummary;

public interface LogSummaryProcedureRepository {
    void insertLogViaProcedure(LogSummary summary);
}
