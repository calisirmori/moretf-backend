package com.moretf.service;

import com.moretf.dto.UserProfileDTO.RecentMatch;
import com.moretf.repository.recentmatch.LogListRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogsListService {

    private final LogListRepository logListRepository;

    public LogsListService(LogListRepository logListRepository) {
        this.logListRepository = logListRepository;
    }

    public List<RecentMatch> getPagedMatches(
            String id64,
            int page,
            int pageSize,
            String playerClass,
            String map,
            String format,
            Long after,
            Long before,
            String teammate,
            String sortBy,
            String sortOrder
    ) {
        int offset = (page - 1) * pageSize;
        return logListRepository.findPagedMatches(
                id64, offset, pageSize, playerClass, map, format, after, before, teammate, sortBy, sortOrder
        );
    }

    public long getMatchCount(
            String id64,
            String playerClass,
            String map,
            String format,
            Long after,
            Long before,
            String teammate
    ) {
        return logListRepository.countMatches(id64, playerClass, map, format, after, before, teammate);
    }

}
