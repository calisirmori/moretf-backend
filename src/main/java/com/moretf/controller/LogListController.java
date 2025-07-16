package com.moretf.controller;

import com.moretf.dto.UserProfileDTO.RecentMatch;
import com.moretf.service.LogsListService;
import org.springframework.web.bind.annotation.*;
import com.moretf.dto.PagedResponse;

import java.util.List;

@RestController
@RequestMapping("/logs")
public class LogListController {

    private final LogsListService logsListService;

    public LogListController(LogsListService logsListService) {
        this.logsListService = logsListService;
    }

    @GetMapping
    public PagedResponse<RecentMatch> getMatches(
            @RequestParam String id64,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int pageSize,
            @RequestParam(required = false) String playerClass,
            @RequestParam(required = false) String map,
            @RequestParam(required = false) String format,
            @RequestParam(required = false) Long after,
            @RequestParam(required = false) Long before,
            @RequestParam(required = false) String teammate,
            @RequestParam(defaultValue = "logDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder
    ) {
        List<RecentMatch> matches = logsListService.getPagedMatches(
                id64, page, pageSize, playerClass, map, format, after, before, teammate, sortBy, sortOrder
        );

        long total = logsListService.getMatchCount(
                id64, playerClass, map, format, after, before, teammate
        );

        return new PagedResponse<>(matches, total);
    }

}
