package com.moretf.model.module.teams;

import com.moretf.model.module.match.RoundInfo;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class TeamSummary {
    private int kills;
    private int deaths;
    private int assists;
    private int damage;
    private int drops;
    private int healing;
    private int charges;
    private int caps;
    private int midFights;

    public void incrementKills() {
        this.kills++;
    }

    public void incrementDeaths() {
        this.deaths++;
    }

    public void incrementAssists() {
        this.assists++;
    }

    public void incrementDrops() {
        this.drops++;
    }

    public void incrementCaps() {
        this.caps++;
    }

    public void incrementCharges() {
        this.charges++;
    }

    public void incrementMidFights() {
        this.midFights++;
    }

    public void addDamage(int damage) {
        this.damage += damage;
    }

    public void addHealing(int healing) {
        this.healing += healing;
    }
}
