package com.moretf.model.module.player;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WeaponStats {
    private int kills;
    private int damage;
    private int shots;
    private int hits;

    public void addDamage(int dmg) {
        this.damage += dmg;
    }

    public void incrementKills(){
        this.kills++;
    }

    public void incrementShots(){
        this.shots++;
    }

    public void incrementHits(){
        this.hits++;
    }
}
