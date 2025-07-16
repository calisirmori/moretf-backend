package com.moretf.service;

import com.moretf.dto.UserProfileDTO.DailyActivity;
import java.util.List;

public interface ActivityService {
    List<DailyActivity> getRecentActivity(String id64);
}
