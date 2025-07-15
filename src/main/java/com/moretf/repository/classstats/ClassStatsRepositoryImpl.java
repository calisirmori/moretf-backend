package com.moretf.repository.classstats;

import com.moretf.dto.UserProfileDTO.ClassStats;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class ClassStatsRepositoryImpl implements ClassStatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, ClassStats> findTopByUserId(String id64) {
        String sql = "SELECT * FROM get_class_stats(:id64)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);

        List<Object[]> resultList = query.getResultList();
        Map<String, ClassStats> statsMap = new LinkedHashMap<>(); // preserves order

        for (Object[] row : resultList) {
            ClassStats stats = new ClassStats();
            stats.classid = (String) row[0];
            stats.count = ((Number) row[1]).intValue();
            stats.wins = ((Number) row[2]).intValue();
            stats.loss = ((Number) row[3]).intValue();
            stats.ties = ((Number) row[4]).intValue();
            stats.classTime = ((Number) row[5]).longValue();

            statsMap.put(stats.classid, stats);
        }

        return statsMap;
    }

}
