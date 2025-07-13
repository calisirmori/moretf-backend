package com.moretf.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/matches")
@RequiredArgsConstructor
public class MatchController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/window")
    public List<Map<String, Object>> getMatchesFromView() {
        String sql = "SELECT * FROM matches_window";
        return jdbcTemplate.queryForList(sql);
    }
}
