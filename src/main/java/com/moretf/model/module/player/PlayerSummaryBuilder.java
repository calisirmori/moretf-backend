package com.moretf.model.module.player;

import com.moretf.model.LogEvent;

import java.io.Console;
import java.util.*;
import java.util.stream.Collectors;

public class PlayerSummaryBuilder {
    public static List<PlayerSummary> build(List<LogEvent> events) {
        Map<String, PlayerSummary> players = new HashMap<>();
        List<UberWindow> activeUbers = new ArrayList<>();

        boolean gameIsActive = false;
        boolean gameIsPaused = false;
        int count = 0;
        for (LogEvent event : events) {

            switch (event.getEventType()) {
                case "round_start":
                    gameIsActive = true;
                    break;

                case "round_win":
                case "round_stalemate":
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
            String name = event.getActor().getName();
            String team = event.getActor().getTeam();

            players.compute(steamId, (key, existing) -> {
                String newTeam = (event.getEventType().equals("team_change") || event.getEventType().equals("joined_team"))
                        ? event.getNewTeam()
                        : team;

                if (existing == null) {
                    return new PlayerSummary(name, steamId, newTeam);
                } else {
                    // Always update if this is an explicit team change event
                    if ((event.getEventType().equals("team_change") || event.getEventType().equals("joined_team"))
                            && newTeam != null && (newTeam.equals("Red") || newTeam.equals("Blue"))) {
                        existing.setTeam(newTeam);
                    }
                    return existing;
                }
            });

            if (gameIsActive) {
                PlayerSummary actingPlayer = players.get(steamId);
                PlayerSummary targetPlayer = null;
                String attackerId = actingPlayer.getSteamId();
                String victimId = null;

                if (event.getTarget() != null) {
                    String targetSteamId = event.getTarget().getSteamId();
                    targetPlayer = players.get(targetSteamId);
                    if (targetPlayer != null) {
                        victimId = targetPlayer.getSteamId();
                    } else {
                        continue;
                    }
                }
                switch (event.getEventType()) {
                    case "kill":
                        //Primary Kill Stats
                        actingPlayer.incrementKills();
                        if (targetPlayer != null) targetPlayer.incrementDeaths();

                        //Class Specific Kill/Death Stats
                        String actingPlayerClassName = actingPlayer.getCharacter();
                        ClassStats actingPlayerClassStats = actingPlayer.getClassStats().computeIfAbsent(actingPlayerClassName, k -> {
                            ClassStats stats = new ClassStats();
                            stats.setClassType(k);
                            return stats;
                        });
                        actingPlayerClassStats.incrementKills();

                        String targetPlayerClassName = targetPlayer.getCharacter();
                        ClassStats targetPlayerClassStats = targetPlayer.getClassStats().computeIfAbsent(targetPlayerClassName, k -> {
                            ClassStats stats = new ClassStats();
                            stats.setClassType(k);
                            return stats;
                        });
                        targetPlayerClassStats.incrementDeaths();


                        // Track Weapon Kills
                        String weapon = event.getWeapon();
                        if (weapon != null && !weapon.isEmpty()) {
                            Map<String, WeaponStats> weaponMap = actingPlayerClassStats.getWeaponStats();

                            WeaponStats weaponStats = weaponMap.computeIfAbsent(weapon, w -> new WeaponStats());
                            weaponStats.incrementKills();
                        }

                        // Track Kill Spread
                        Map<String, Integer> killSpread = actingPlayer.getKillSpread();
                        killSpread.put(victimId, killSpread.getOrDefault(victimId, 0) + 1);

                        // Track Death Spread
                        Map<String, Integer> deathSpread = targetPlayer.getDeathSpread();
                        deathSpread.put(attackerId, deathSpread.getOrDefault(attackerId, 0) + 1);

                        // Track Kills by Class
                        String victimClass = targetPlayer.getCharacter();
                        actingPlayer.getKillsByClass().put(
                                victimClass,
                                actingPlayer.getKillsByClass().getOrDefault(victimClass, 0) + 1
                        );

                        // Track Death by Class
                        if (targetPlayer != null) {
                            String actingClass = actingPlayer.getCharacter();
                            targetPlayer.getDeathsByClass().put(
                                    actingClass,
                                    targetPlayer.getDeathsByClass().getOrDefault(actingClass, 0) + 1
                            );
                        }

                        // Deaths during Uber
                        long killTime = event.getTimestamp();
                        String victimTeam = targetPlayer.getTeam();

                        boolean diedDuringEnemyUber = false;
                        for (UberWindow uber : activeUbers) {
                            if (uber.team != null && !uber.team.equals(victimTeam) && uber.isWithinWindow(killTime)) {
                                diedDuringEnemyUber = true;
                                break;
                            }
                        }

                        if (diedDuringEnemyUber) {
                            targetPlayer.incrementDeathsDuringUber();
                        }

                        break;

                    case "kill_assist":
                        actingPlayer.incrementAssists();

                        //Class Specific Assist Stats
                        String assistingPlayerClassType = actingPlayer.getCharacter();
                        ClassStats assistingPlayerClassStats = actingPlayer.getClassStats().computeIfAbsent(assistingPlayerClassType, k -> {
                            ClassStats s = new ClassStats();
                            s.setClassType(assistingPlayerClassType);
                            return s;
                        });
                        assistingPlayerClassStats.incrementAssists();

                        //Track Assist by Class
                        victimClass = targetPlayer.getCharacter();
                        actingPlayer.getAssistByClass().put(
                                victimClass,
                                actingPlayer.getAssistByClass().getOrDefault(victimClass, 0) + 1
                        );

                        break;

                    case "damage":

                        int damageDealt = event.getDamage() > 450 ? 450 : event.getDamage();

                        // Primary Damage and Damage Taken statlines
                        actingPlayer.addDamage(damageDealt);
                        if (targetPlayer != null) targetPlayer.addTaken(damageDealt);

                        // Class Specific Damage and Damage Taken Stats
                        String activeClass = actingPlayer.getCharacter();
                        ClassStats classStats = actingPlayer.getClassStats().get(activeClass);
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

                        //Headshot Event
                        Object airshotFlag = event.getExtras() != null ? event.getExtras().get("airshot") : null;
                        if ("1".equals(String.valueOf(airshotFlag))) {
                            actingPlayer.incrementAirShots();
                        }

                        //Backstab Event
                        Object critFlag = event.getExtras() != null ? event.getExtras().get("crit") : null;
                        if ("crit".equals(String.valueOf(critFlag)) && "spy".equals(actingPlayer.getCharacter()) && !"1".equals(String.valueOf(headshotFlag))) {
                            actingPlayer.incrementBackStabs();
                        }

                        // Track damage dealt by this player to the target
                        Map<String, Integer> dealtMap = actingPlayer.getDamageDealtSpread();
                        dealtMap.put(victimId, dealtMap.getOrDefault(victimId, 0) + damageDealt);

                        // Track damage taken by the target from this player
                        Map<String, Integer> takenMap = targetPlayer.getDamageTakenSpread();
                        takenMap.put(attackerId, takenMap.getOrDefault(attackerId, 0) + damageDealt);

                        // Healing from damage-based sources (e.g., Black Box, Crossbow, etc.)
                        Integer heal = event.getHealing();
                        if (heal != null && heal > 0) {
                            actingPlayer.addHealing(heal);

                            if (targetPlayer != null) {
                                Map<String, Integer> healingMap = targetPlayer.getHealedBySource();
                                healingMap.put(actingPlayer.getSteamId(), healingMap.getOrDefault(actingPlayer.getSteamId(), 0) + heal);
                            }

                            Map<String, Integer> healedMap = actingPlayer.getHealingDoneSpread();
                            healedMap.put(victimId, healedMap.getOrDefault(victimId, 0) + heal);
                        }
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

                    case "chargedeployed": {
                        UberWindow window = new UberWindow(
                                actingPlayer.getSteamId(),
                                actingPlayer.getTeam(),
                                event.getTimestamp()
                        );
                        activeUbers.add(window);

                        String weaponName = event.getWeapon();
                        Map<String, Integer> ubers = actingPlayer.getUbers();
                        ubers.put(weaponName, ubers.getOrDefault(weaponName, 0) + 1);

                        String uberTeam = actingPlayer.getTeam();
                        long uberTime = event.getTimestamp();

                        // Look backward for kills within 5 seconds before Uber started
                        // Look backward for kills within 5 seconds before Uber started
                        for (LogEvent potentialKill : events) {
                            if (!"kill".equals(potentialKill.getEventType())) continue;

                            long killTimeStamp = potentialKill.getTimestamp();  // Only declare once here

                            if (killTimeStamp < uberTime && (uberTime - killTimeStamp) <= 5000) {
                                if (potentialKill.getTarget() != null) {
                                    victimId = potentialKill.getTarget().getSteamId();
                                    victimTeam = potentialKill.getTarget().getTeam();

                                    if (uberTeam != null && !uberTeam.equals(victimTeam)) {
                                        PlayerSummary victim = players.get(victimId);
                                        if (victim != null) {
                                            victim.incrementDeathsBeforeUber();
                                        }
                                    }
                                }
                            }
                        }


                        break;
                    }


                    case "chargeended":

                        long endTime = event.getTimestamp();
                        String medicId = event.getActor().getSteamId();

                        for (UberWindow activeWindow  : activeUbers) {
                            if (activeWindow.medicSteamId.equals(medicId) && activeWindow.endTime == -1) {
                                activeWindow.endTime = endTime;
                                break;
                            }
                        }

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
                        if (event.getUberPercentage() >= 90){
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
                    case "joined_team":
                    case "team_change": {
                        // Handle team assignment override
                        String newTeam = event.getNewTeam();
                        if (newTeam != null && (newTeam.equals("Red") || newTeam.equals("Blue"))) {
                            actingPlayer.setTeam(newTeam);
                        }
                        break;
                    }

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

                    case "player_builtobject": {
                        actingPlayer.incrementObjectsBuilt();
                        break;
                    }

                    case "killedobject": {
                        actingPlayer.incrementObjectsDestroyed();
                        break;
                    }

                    case "pointcaptured": {
                        Object cappersObj = event.getExtras() != null ? event.getExtras().get("cappers") : null;

                        if (cappersObj instanceof List<?>) {
                            List<?> cappers = (List<?>) cappersObj;

                            for (Object capperObj : cappers) {
                                if (!(capperObj instanceof Map<?, ?>)) continue;
                                Map<?, ?> capper = (Map<?, ?>) capperObj;

                                Object steamIdObj = capper.get("steamId");
                                if (!(steamIdObj instanceof String)) continue;

                                String capperId = (String) steamIdObj;
                                PlayerSummary capperPlayer = players.get(capperId);
                                if (capperPlayer != null) {
                                    capperPlayer.incrementCaptures();
                                }
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

        activeUbers.clear();

        return players.values().stream()
                .filter(player -> player.getTotalTime() >= 10 && !player.getCharacter().equals("undefined"))
                .collect(Collectors.toList());
    }
}
