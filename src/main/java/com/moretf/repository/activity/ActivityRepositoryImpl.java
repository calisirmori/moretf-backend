package com.moretf.repository.activity;

import com.moretf.dto.UserProfileDTO.DailyActivity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class ActivityRepositoryImpl implements ActivityRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<DailyActivity> findRecentActivity(String id64) {
        String sql = "SELECT * FROM get_player_activity_3mo(:id64)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);

        List<Object[]> rows = query.getResultList();
        List<DailyActivity> activityList = new ArrayList<>();

        for (Object[] row : rows) {
            DailyActivity a = new DailyActivity();
            a.activityDate = row[0].toString();            a.totalMatches = ((Number) row[1]).intValue();
            a.wins = ((Number) row[2]).intValue();
            a.losses = ((Number) row[3]).intValue();
            a.ties = ((Number) row[4]).intValue();
            activityList.add(a);
        }

        return activityList;
    }
}
