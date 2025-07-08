package com.moretf.model.module.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClassStats {
    private String classType; // e.g., "pyro"
    private int kills;
    private int assists;
    private int deaths;
    private int damage;
    private int totalTime;

    private Map<String, WeaponStats> weaponStats = new HashMap<>(); // e.g., "flamethrower"

    public void incrementKills(){
        this.kills++;
    }

    public void incrementDeaths(){
        this.deaths++;
    }

    public void incrementAssists(){
        this.assists++;
    }

    public void addDamage(int damage){
        this.damage += damage;
    }
}
