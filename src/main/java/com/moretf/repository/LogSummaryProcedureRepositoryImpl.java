package com.moretf.repository;

import com.moretf.LogMetaData.LogSummary;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.StoredProcedureQuery;
import org.springframework.stereotype.Repository;

@Repository
public class LogSummaryProcedureRepositoryImpl implements LogSummaryProcedureRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void insertLogViaProcedure(LogSummary summary) {
        StoredProcedureQuery query = entityManager.createStoredProcedureQuery("public.insert_log");

        query.registerStoredProcedureParameter("_logid", Long.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_title", String.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_match_length", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_redscore", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_bluescore", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_players", Integer.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_format", String.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_map", String.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_log_date", Long.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_game_type", String.class, jakarta.persistence.ParameterMode.IN);
        query.registerStoredProcedureParameter("_combined", String.class, jakarta.persistence.ParameterMode.IN);

        query.setParameter("_logid", summary.getLogid());
        query.setParameter("_title", summary.getTitle());
        query.setParameter("_match_length", summary.getMatchLength());
        query.setParameter("_redscore", (int) summary.getRedscore());
        query.setParameter("_bluescore", (int) summary.getBluescore());
        query.setParameter("_players", summary.getPlayers());
        query.setParameter("_format", summary.getFormat());
        query.setParameter("_map", summary.getMap());
        //During backlogging use match end date
        query.setParameter("_log_date", summary.getLogDate());
        //During production use log upload date
        //query.setParameter("_log_date", summary.getLogUploadDate());
        query.setParameter("_game_type", "None");
        query.setParameter("_combined", summary.getCombined());

        query.execute();
    }
}
