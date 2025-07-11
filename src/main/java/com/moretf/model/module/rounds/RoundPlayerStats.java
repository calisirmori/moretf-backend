package com.moretf.model.module.rounds;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RoundPlayerStats {
    private final String team;
    private int kills = 0;
    private int dmg = 0;

    @JsonCreator
    public RoundPlayerStats(
            @JsonProperty("team") String team
    ) {
        this.team = team;
    }

    public void incrementKills() { kills++; }
    public void addDmg(int dmg) { this.dmg += dmg; }
}
