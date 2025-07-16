package com.moretf.repository.activity;

import com.moretf.dto.UserProfileDTO.DailyActivity;
import java.util.List;

public interface ActivityRepository {
    List<DailyActivity> findRecentActivity(String id64);
    List<DailyActivity> findAllActivity(String id64);
}
