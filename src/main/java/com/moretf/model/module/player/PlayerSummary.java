package com.moretf.model.module.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class PlayerSummary {
    private String name;
    private String steamId;
    private String team;
    private int kills;

    public PlayerSummary(String name, String steamId, String team) {
        this.name = name;
        this.steamId = steamId;
        this.team = team;
        this.kills = 0;
    }

    public void incrementKills() {
        this.kills++;
    }
}
