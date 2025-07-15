package com.moretf.repository;

import com.moretf.dto.UserProfileDTO.MapStats;
import java.util.Map;

public interface MapStatsRepository {
    Map<String, MapStats> findByUserId(String id64);
}
