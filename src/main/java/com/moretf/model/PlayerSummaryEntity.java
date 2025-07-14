package com.moretf.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PlayerSummaryEntity {
    private int kills, assists, deaths, dpm, damage, dtm, dt, heals;
    private String playerClass, team, playerName, matchResult;
    private long id64;
    private long logid;
}
