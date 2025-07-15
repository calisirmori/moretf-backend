package com.moretf.repository.recentmatch;

import com.moretf.dto.UserProfileDTO.RecentMatch;
import java.util.List;

public interface RecentMatchRepository {
    List<RecentMatch> findByUserId(String id64);
}
