package com.moretf.repository;

import com.moretf.dto.UserProfileDTO.DailyActivity;
import java.util.List;

public interface ActivityRepository {
    List<DailyActivity> findRecentActivity(String id64);
}
