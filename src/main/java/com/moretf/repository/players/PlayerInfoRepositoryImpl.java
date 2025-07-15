package com.moretf.repository.players;

import com.moretf.dto.UserProfileDTO;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class PlayerInfoRepositoryImpl implements PlayerInfoRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public UserProfileDTO.ProfileAndStats getProfileAndStats(String id64) {
        String sql = "SELECT * FROM get_player_profile_and_stats(:id64)";
        Query query = entityManager.createNativeQuery(sql);
        query.setParameter("id64", id64);
        Object[] row = (Object[]) query.getSingleResult();

        UserProfileDTO.ProfileAndStats result = new UserProfileDTO.ProfileAndStats();

        // Profile section
        result.profile = new UserProfileDTO.Profile();
        result.profile.steamName = (String) row[0];
        result.profile.avatar = (String) row[1];
        result.profile.rglName = (String) row[2];
        result.profile.etf2lName = (String) row[3];
        result.profile.ozfName = (String) row[4];
        result.profile.etf2lId = row[5] != null ? row[5].toString() : null;
        result.profile.ozfId = row[6] != null ? row[6].toString() : null;

        // OverallStats section
        result.overallStats = new UserProfileDTO.OverallStats();
        result.overallStats.count = ((Number) row[8]).intValue();
        result.overallStats.wins = ((Number) row[9]).intValue();
        result.overallStats.loss = ((Number) row[10]).intValue();
        result.overallStats.ties = ((Number) row[11]).intValue();
        result.overallStats.totalTime = row[7] != null ? ((Number) row[7]).doubleValue() : 0.0;

        return result;
    }
}
