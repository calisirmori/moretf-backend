package com.moretf.model.module.timeline;

public class PlayerAggregate {
    public int damage = 0;
    public int healing = 0;
    public int kills = 0;
    public int deaths = 0;
    public int ubers = 0;
    public String team;

    public PlayerAggregate(String team) {
        this.team = team;
    }
}