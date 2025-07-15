package com.moretf.repository.mapstats;

import com.moretf.dto.UserProfileDTO.MapStats;
import java.util.Map;

public interface MapStatsRepository {
    Map<String, MapStats> findByUserId(String id64);
}
