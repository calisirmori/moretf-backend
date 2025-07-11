package com.moretf.model.module.player;

public class UberWindow {
    String medicSteamId;
    String team;
    long startTime;
    long endTime; // initially -1 until closed

    UberWindow(String medicSteamId, String team, long startTime) {
        this.medicSteamId = medicSteamId;
        this.team = team;
        this.startTime = startTime;
        this.endTime = -1;
    }

    boolean isWithinWindow(long timestamp) {
        return startTime <= timestamp && (endTime == -1 || timestamp <= endTime);
    }
}
