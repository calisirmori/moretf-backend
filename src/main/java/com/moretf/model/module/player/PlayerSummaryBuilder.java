package com.moretf.model.module.player;

import com.moretf.model.LogEvent;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerSummaryBuilder {
    public static List<PlayerSummary> build(List<LogEvent> events) {
        Map<String, PlayerSummary> players = new HashMap<>();

        boolean gameIsActive = false;
        boolean gameIsPaused = false;
        for (LogEvent event : events) {

            switch (event.getEventType()) {
                case "round_start":
                    gameIsActive = true;
                    break;

                case "round_win":
                case "game_over":
                    gameIsActive = false;
                    break;

                case "game_unpaused":
                    gameIsPaused = false;
                    break;

                case "game_paused":
                    gameIsPaused = true;
                    break;
            }

            if (event.getActor() == null || event.getActor().getSteamId() == null) continue;
            String steamId = event.getActor().getSteamId();
            players.putIfAbsent(steamId,
                    new PlayerSummary(
                            event.getActor().getName(),
                            steamId,
                            event.getActor().getTeam()
                    )
            );

            if (gameIsActive && !gameIsPaused) {
                PlayerSummary actingPlayer = players.get(steamId);
                PlayerSummary targetPlayer = null;
                String attackerId = actingPlayer.getSteamId();
                String victimId = null;

                if (event.getTarget() != null){
                    targetPlayer = players.get(event.getTarget().getSteamId());
                    victimId = targetPlayer.getSteamId();
                }

                switch (event.getEventType()) {
                    case "kill":
                        //Primary Kill Stats
                        actingPlayer.incrementKills();
                        if (targetPlayer != null) targetPlayer.incrementDeaths();

                        //Class Specific Kill/Death Stats
                        actingPlayer.getClassStats().get(actingPlayer.getCharacter()).incrementKills();
                        if (targetPlayer != null) targetPlayer.getClassStats().get(targetPlayer.getCharacter()).incrementDeaths();

                        // Track Weapon Kills
                        String activeClass = actingPlayer.getCharacter();
                        ClassStats classStats = actingPlayer.getClassStats().get(activeClass);
                        String weapon = event.getWeapon();
                        if (weapon != null && !weapon.isEmpty()) {
                            Map<String, WeaponStats> weaponMap = classStats.getWeaponStats();

                            WeaponStats weaponStats = weaponMap.computeIfAbsent(weapon, w -> new WeaponStats());
                            weaponStats.incrementKills();
                        }

                        // Track Kill Spread
                        Map<String, Integer> killSpread = actingPlayer.getKillSpread();
                        killSpread.put(victimId, killSpread.getOrDefault(victimId, 0) + 1);

                        // Track Death Spread
                        Map<String, Integer> deathSpread = targetPlayer.getDeathSpread();
                        deathSpread.put(attackerId, deathSpread.getOrDefault(attackerId, 0) + 1);

                        break;

                    case "kill_assist":
                        actingPlayer.incrementAssists();

                        //Class Specific Assist Stats
                        actingPlayer.getClassStats().get(actingPlayer.getCharacter()).incrementAssists();
                        break;

                    case "damage":

                        int damageDealt = event.getDamage() > 450 ? 450 : event.getDamage();

                        // Primary Damage and Damage Taken statlines
                        actingPlayer.addDamage(damageDealt);
                        if (targetPlayer != null) targetPlayer.addTaken(damageDealt);

                        // Class Specific Damage and Damage Taken Stats
                        activeClass = actingPlayer.getCharacter();
                        classStats = actingPlayer.getClassStats().get(activeClass);
                        if (classStats != null) {
                            classStats.addDamage(damageDealt);

                            // Weapon Damage
                            weapon = event.getWeapon();
                            if (weapon != null && !weapon.isEmpty()) {
                                Map<String, WeaponStats> weaponMap = classStats.getWeaponStats();

                                WeaponStats weaponStats = weaponMap.computeIfAbsent(weapon, w -> new WeaponStats());
                                weaponStats.addDamage(damageDealt);
                            }
                        }

                        //Headshot Event
                        Object headshotFlag = event.getExtras() != null ? event.getExtras().get("headshot") : null;
                        if ("1".equals(String.valueOf(headshotFlag))) {
                            actingPlayer.incrementHeadShots();
                        }

                        // Track damage dealt by this player to the target
                        Map<String, Integer> dealtMap = actingPlayer.getDamageDealtSpread();
                        dealtMap.put(victimId, dealtMap.getOrDefault(victimId, 0) + damageDealt);

                        // Track damage taken by the target from this player
                        Map<String, Integer> takenMap = targetPlayer.getDamageTakenSpread();
                        takenMap.put(attackerId, takenMap.getOrDefault(attackerId, 0) + damageDealt);

                        break;

                    case "item_pickup":
                        String itemName = event.getItem();

                        // Count pickup
                        Map<String, Integer> items = actingPlayer.getItemPickups();
                        items.put(itemName, items.getOrDefault(itemName, 0) + 1);

                        // Track healing from item (if applicable)
                        Integer healing = event.getHealing();
                        if (healing != null) {
                            Map<String, Integer> healingMap = actingPlayer.getHealedBySource();
                            healingMap.put(itemName, healingMap.getOrDefault(itemName, 0) + healing);
                        }

                        break;

                    //Medic Events
                    case "healed":
                        // Primary Healing
                        int healingDone = event.getHealing();

                        actingPlayer.addHealing(healingDone);

                        String actingPlayerID = event.getActor().getSteamId();
                        Map<String, Integer> healingMap = targetPlayer.getHealedBySource();
                        healingMap.put(actingPlayerID, healingMap.getOrDefault(actingPlayerID, 0) + healingDone);

                        Map<String, Integer> healedMap = actingPlayer.getHealingDoneSpread();
                        healedMap.put(victimId, healedMap.getOrDefault(victimId, 0) + healingDone);

                        break;

                    case "chargedeployed":
                        String weaponName = event.getWeapon();
                        Map<String, Integer> ubers = actingPlayer.getUbers();
                        ubers.put(weaponName, ubers.getOrDefault(weaponName, 0) + 1);
                        break;

                    case "chargeended":
                        double uberLength = Double.parseDouble(event.getExtras().get("duration").toString());
                        actingPlayer.addTotalUberLength(uberLength);
                        break;

                    case "medic_death":
                        if (Integer.parseInt(event.getUbercharge()) == 1){
                            actingPlayer.incrementMedDrops();
                            if (targetPlayer != null) {
                                targetPlayer.incrementDrops();
                            }
                        }
                        break;

                    case "medic_death_ex":
                        if (Integer.parseInt(event.getExtras().get("uberpct").toString()) >= 90){
                            actingPlayer.incerementNearChargeDeaths();
                        }
                        break;

                    case "spawn":
                        String classType = event.getCharacter();
                        long eventTimestamp = event.getTimestamp();

                        // Create class entry if missing
                        actingPlayer.getClassStats().computeIfAbsent(classType, k -> {
                            ClassStats stats = new ClassStats();
                            stats.setClassType(classType);
                            return stats;
                        });

                        // If player has no character yet, assign it and set classStartTime
                        if ("unknown".equals(actingPlayer.getCharacter())) {
                            actingPlayer.setCharacter(classType);
                            actingPlayer.setClassStartTime(eventTimestamp);
                        }
                        break;

                    case "class_change":
                        String newClass = event.getCharacter();
                        long timestamp = event.getTimestamp();

                        // Finalize previous class time
                        String oldClass = actingPlayer.getCharacter();
                        if (!"unknown".equals(oldClass) && actingPlayer.getClassStartTime() != -1) {
                            ClassStats oldStats = actingPlayer.getClassStats().get(oldClass);
                            if (oldStats != null) {
                                int delta = (int) ((timestamp - actingPlayer.getClassStartTime()) / 1000);
                                oldStats.setTotalTime(oldStats.getTotalTime() + delta);
                            }
                        }

                        // Switch to new class
                        actingPlayer.setCharacter(newClass);
                        actingPlayer.setClassStartTime(timestamp);

                        // Create new class record if needed
                        actingPlayer.getClassStats().computeIfAbsent(newClass, k -> {
                            ClassStats stats = new ClassStats();
                            stats.setClassType(newClass);
                            return stats;
                        });
                        break;

                    case "shot_fired": {
                        String currentClass = actingPlayer.getCharacter();
                        weapon = event.getWeapon();

                        if (currentClass != null && weapon != null) {
                            classStats = actingPlayer.getClassStats().get(currentClass);
                            if (classStats != null) {
                                WeaponStats weaponStats = classStats.getWeaponStats().computeIfAbsent(weapon, w -> new WeaponStats());
                                weaponStats.incrementShots();
                            }
                        }
                        break;
                    }

                    case "shot_hit": {
                        String currentClass = actingPlayer.getCharacter();
                        weapon = event.getWeapon();

                        if (currentClass != null && weapon != null) {
                            classStats = actingPlayer.getClassStats().get(currentClass);
                            if (classStats != null) {
                                WeaponStats weaponStats = classStats.getWeaponStats().computeIfAbsent(weapon, w -> new WeaponStats());
                                weaponStats.incrementHits();
                            }
                        }
                        break;
                    }

                }
            }
        }
        // Calculations after game ended
        if (!events.isEmpty()) {
            long lastTimestamp = events.get(events.size() - 1).getTimestamp();

            for (PlayerSummary player : players.values()) {
                String currentClass = player.getCharacter();
                if (!"unknown".equals(currentClass) && player.getClassStartTime() != -1) {
                    ClassStats stats = player.getClassStats().get(currentClass);
                    if (stats != null) {
                        int deltaSeconds = (int) ((lastTimestamp - player.getClassStartTime()) / 1000);
                        stats.setTotalTime(stats.getTotalTime() + deltaSeconds);
                    }
                }

                player.setClassStartTime(-1);

                // Determine most played class and total combined playtime
                String mostPlayedClass = "unknown";
                int maxTime = 0;
                int totalCombinedTime = 0;

                for (Map.Entry<String, ClassStats> entry : player.getClassStats().entrySet()) {
                    int classTime = entry.getValue().getTotalTime();
                    totalCombinedTime += classTime;

                    if (classTime > maxTime) {
                        maxTime = classTime;
                        mostPlayedClass = entry.getKey();
                    }
                }

                player.setCharacter(mostPlayedClass);
                player.setTotalTime(totalCombinedTime);
            }
        }


        return players.values().stream()
                .filter(player -> player.getTotalTime() >= 10)
                .collect(Collectors.toList());
    }
}
