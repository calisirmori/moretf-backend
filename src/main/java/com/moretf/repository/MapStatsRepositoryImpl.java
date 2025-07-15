package com.moretf.repository;

import com.moretf.dto.UserProfileDTO.MapStats;
import jakarta.persistence.*;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Repository
public class MapStatsRepositoryImpl implements MapStatsRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<String, MapStats> findByUserId(String id64) {
        String sql = "SELECT * FROM get_map_stats(:id64)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);

        List<Object[]> resultList = query.getResultList();
        Map<String, MapStats> mapStatsMap = new LinkedHashMap<>(); // 👈 preserves order

        for (Object[] row : resultList) {
            String mapName = (String) row[0];

            MapStats stats = new MapStats();
            stats.count = ((Number) row[1]).intValue();
            stats.wins = ((Number) row[2]).intValue();
            stats.loss = ((Number) row[3]).intValue();
            stats.ties = ((Number) row[4]).intValue();
            stats.mapTime = ((Number) row[5]).doubleValue();

            mapStatsMap.put(mapName, stats);
        }

        return mapStatsMap;
    }

}
