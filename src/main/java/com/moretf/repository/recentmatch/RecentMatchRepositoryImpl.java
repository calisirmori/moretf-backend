package com.moretf.repository.recentmatch;

import com.moretf.dto.UserProfileDTO.RecentMatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class RecentMatchRepositoryImpl implements RecentMatchRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<RecentMatch> findByUserId(String id64) {
        String sql = "SELECT * FROM get_recent_matches(:id64)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);

        List<Object[]> rows = query.getResultList();
        List<RecentMatch> matches = new ArrayList<>();

        for (Object[] row : rows) {
            RecentMatch match = new RecentMatch();
            match.logid = ((Number) row[0]).longValue();        // BIGINT
            match.kills = ((Number) row[1]).intValue();
            match.assists = ((Number) row[2]).intValue();
            match.deaths = ((Number) row[3]).intValue();
            match.dpm = ((Number) row[4]).intValue();
            match.dtm = ((Number) row[5]).longValue();          // BIGINT
            match.hpm = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
            match.playerClass = (String) row[7];
            match.team = (String) row[8];
            match.matchResult = (String) row[9];
            match.matchLength = ((Number) row[10]).intValue();
            match.format = (String) row[11];
            match.logDate = ((Number) row[12]).longValue();     // BIGINT
            match.combined = (String) row[13];
            match.map = (String) row[14];
            match.title = (String) row[15];

            matches.add(match);
        }

        return matches;
    }
}
