package com.moretf.repository.recentmatch;

import com.moretf.dto.UserProfileDTO.RecentMatch;
import java.util.List;

public interface LogListRepository {
    List<RecentMatch> findByUserId(String id64);

    List<RecentMatch> findPagedMatches(
            String id64,
            int offset,
            int limit,
            String playerClass,
            String map,
            String format,
            Long after,
            Long before,
            String teammateId,
            String sortBy,
            String sortOrder
    );

    long countMatches(
            String id64,
            String playerClass,
            String map,
            String format,
            Long after,
            Long before,
            String teammateId
    );

}
