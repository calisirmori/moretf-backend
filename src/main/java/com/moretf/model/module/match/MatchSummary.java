package com.moretf.model.module.match;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchSummary {
    private int id;
    private String title;
    private String map;
    private Long matchStartTime;
    private String winner;
    private int scoreRed;
    private int scoreBlue;
    private int durationSeconds;
    private boolean combined;
    private Map<Integer, RoundInfo> rounds;
}
