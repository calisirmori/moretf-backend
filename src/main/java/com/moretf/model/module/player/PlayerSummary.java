package com.moretf.model.module.player;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
public class PlayerSummary {
    private String name;
    private String steamId;
    private String steamId64;
    private String team;
    private int kills;
    private int assists;
    private int deaths;
    private int damage;
    private int damageTaken;
    private int healing;
    private Map<String, Integer> ubers = new HashMap<>();
    private int drops;
    private int medDrops;
    private int nearChargeDeaths;
    private double totalUberLength;
    private Map<String, Integer> itemPickups = new HashMap<>();
    private int headShots;
    private int backStabs;
    private int captures;
    private int objectsBuilt;
    private int objectsDestroyed;
    private int airShots;
    private int deathsBeforeUber;
    private int deathsDuringUber;
    private String character;
    private Map<String, ClassStats> classStats = new HashMap<>();
    private long classStartTime;

    private Map<String, Integer> healedBySource = new HashMap<>();
    private Map<String, Integer> damageDealtSpread = new HashMap<>();
    private Map<String, Integer> damageTakenSpread = new HashMap<>();
    private Map<String, Integer> healingDoneSpread = new HashMap<>();
    private Map<String, Integer> killSpread = new HashMap<>();
    private Map<String, Integer> deathSpread = new HashMap<>();

    private Map<String, Integer> killsByClass = new HashMap<>();
    private Map<String, Integer> assistByClass = new HashMap<>();
    private Map<String, Integer> deathsByClass = new HashMap<>();

    private int totalTime;

    public PlayerSummary(String name, String steamId, String team) {
        this.name = name;
        this.steamId = steamId;
        this.steamId64 = steamId3ToSteamId64(steamId);
        this.team = team;
        this.kills = 0;
        this.assists = 0;
        this.deaths = 0;
        this.damage = 0;
        this.damageTaken = 0;
        this.healing = 0;
        this.ubers = new HashMap<>();
        this.drops = 0;
        this.medDrops = 0;
        this.nearChargeDeaths = 0;
        this.totalUberLength = 0.0;
        this.headShots = 0;
        this.backStabs = 0;
        this.captures = 0;
        this.objectsBuilt = 0;
        this.objectsDestroyed = 0;
        this.airShots = 0;
        this.objectsDestroyed = 0;
        this.deathsBeforeUber = 0;
        this.deathsDuringUber = 0;
        this.character = "unknown";
        this.classStartTime = -1;
        this.healedBySource = new HashMap<>();
        this.damageTakenSpread = new HashMap<>();
        this.damageDealtSpread = new HashMap<>();
        this.healingDoneSpread = new HashMap<>();
        this.killSpread = new HashMap<>();
        this.deathSpread = new HashMap<>();
        this.totalTime = 0;
    }

    public void incrementKills() {
        this.kills++;
    }

    public void incrementAssists() {
        this.assists++;
    }

    public void incrementDeaths() {
        this.deaths++;
    }

    public void addDamage(int damage) {
        this.damage += damage;
    }

    public void addTaken(int damageTaken) {
        this.damageTaken += damageTaken;
    }

    public void addHealing(int healing) {
        this.healing += healing;
    }

    public void incrementDrops() {
        this.drops++;
    }

    public void incrementMedDrops() {
        this.medDrops++;
    }

    public void incerementNearChargeDeaths() {
        this.nearChargeDeaths++;
    }

    public void addTotalUberLength(double uberLenght) {
        this.totalUberLength += uberLenght;
    }

    public void incrementHeadShots(){
        this.headShots++;
    }

    public void incrementBackStabs(){
        this.backStabs++;
    }

    public void incrementCaptures(){
        this.captures++;
    }

    public void incrementObjectsBuilt(){
        this.objectsBuilt++;
    }

    public void incrementObjectsDestroyed() { this.objectsDestroyed++; }

    public void incrementAirShots(){
        this.airShots++;
    }

    public void incrementDeathsBeforeUber(){this.deathsBeforeUber++;}

    public void incrementDeathsDuringUber(){this.deathsDuringUber++;}

    public static String steamId3ToSteamId64(String steamId3) {
        try {
            String trimmed = steamId3.replace("[U:1:", "").replace("]", "");
            long accountId = Long.parseLong(trimmed);
            return String.valueOf(accountId + 76561197960265728L);
        } catch (Exception e) {
            return null;
        }
    }
}
