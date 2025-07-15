package com.moretf.repository;

import com.moretf.dto.UserProfileDTO.ClassStats;
import java.util.Map;

public interface ClassStatsRepository {
    Map<String, ClassStats> findTopByUserId(String id64);
}
