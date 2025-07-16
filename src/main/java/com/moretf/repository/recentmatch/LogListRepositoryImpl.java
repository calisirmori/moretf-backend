package com.moretf.repository.recentmatch;

import com.moretf.dto.UserProfileDTO.RecentMatch;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class LogListRepositoryImpl implements LogListRepository {

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

    @Override
    public List<RecentMatch> findPagedMatches(
            String id64, int offset, int limit,
            String playerClass, String map, String format,
            Long after, Long before, String teammateId,
            String sortBy, String sortOrder
    ) {
        String sql = "SELECT * FROM get_recent_matches_paged(:id64, :offset, :limit, :class, :map, :format, :after, :before, :teammate, :sort_by, :sort_order)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);
        query.setParameter("offset", offset);
        query.setParameter("limit", limit);
        query.setParameter("class", playerClass);
        query.setParameter("map", map);
        query.setParameter("format", format);
        query.setParameter("after", after);
        query.setParameter("before", before);
        query.setParameter("teammate", teammateId);
        query.setParameter("sort_by", sortBy);
        query.setParameter("sort_order", sortOrder);


        List<Object[]> rows = query.getResultList();
        List<RecentMatch> matches = new ArrayList<>();

        for (Object[] row : rows) {
            RecentMatch match = new RecentMatch();
            match.logid = ((Number) row[0]).longValue();
            match.kills = ((Number) row[1]).intValue();
            match.assists = ((Number) row[2]).intValue();
            match.deaths = ((Number) row[3]).intValue();
            match.dpm = ((Number) row[4]).intValue();
            match.dtm = ((Number) row[5]).longValue();
            match.hpm = row[6] != null ? ((Number) row[6]).doubleValue() : 0.0;
            match.playerClass = (String) row[7];
            match.team = (String) row[8];
            match.matchResult = (String) row[9];
            match.matchLength = ((Number) row[10]).intValue();
            match.format = (String) row[11];
            match.logDate = ((Number) row[12]).longValue();
            match.combined = (String) row[13];
            match.map = (String) row[14];
            match.title = (String) row[15];
            matches.add(match);
        }

        return matches;
    }

    @Override
    public long countMatches(
            String id64,
            String playerClass,
            String map,
            String format,
            Long after,
            Long before,
            String teammateId
    ) {
        String sql = "SELECT get_recent_matches_count(:id64, :class, :map, :format, :after, :before, :teammate)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);
        query.setParameter("class", playerClass);
        query.setParameter("map", map);
        query.setParameter("format", format);
        query.setParameter("after", after);
        query.setParameter("before", before);
        query.setParameter("teammate", teammateId);

        Object result = query.getSingleResult();
        return result != null ? ((Number) result).longValue() : 0;
    }


}
