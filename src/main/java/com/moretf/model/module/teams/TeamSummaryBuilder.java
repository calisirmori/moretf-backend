package com.moretf.model.module.teams;

import com.moretf.model.LogEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TeamSummaryBuilder {

    public static Map<String, TeamSummary> build(List<LogEvent> events) {
        Map<String, TeamSummary> teams = new HashMap<>();
        teams.put("Red", new TeamSummary());
        teams.put("Blue", new TeamSummary());

        boolean gameIsActive = false;
        boolean gameIsPaused = false;
        boolean waitingForMidfightCap = false;
        boolean midfightCaptured = false;

        for (LogEvent event : events) {
            switch (event.getEventType()) {
                case "round_start":
                    gameIsActive = true;
                    waitingForMidfightCap = true;
                    midfightCaptured = false;
                    break;
                case "round_win":
                case "game_over":
                    gameIsActive = false;
                    waitingForMidfightCap = false;
                    break;
                case "game_paused":
                    gameIsPaused = true;
                    break;
                case "game_unpaused":
                    gameIsPaused = false;
                    break;
            }

            if (!gameIsActive || gameIsPaused) continue;

            // --- Handle stats here ---
            String team = event.getActor() != null ? event.getActor().getTeam() : null;
            if (team != null && teams.containsKey(team)) {
                TeamSummary summary = teams.get(team);

                switch (event.getEventType()) {
                    case "kill":
                        summary.incrementKills();
                        String targetTeam = event.getTarget() != null ? event.getTarget().getTeam() : null;
                        if (targetTeam != null && teams.containsKey(targetTeam)) {
                            teams.get(targetTeam).incrementDeaths();
                        }
                        break;
                    case "kill_assist":
                        summary.incrementAssists();
                        break;
                    case "damage":
                        summary.addDamage(Math.min(event.getDamage(), 450));
                        break;
                    case "healed":
                        summary.addHealing(event.getHealing());
                        break;
                    case "chargedeployed":
                        summary.incrementCharges();
                        break;
                    case "drop":
                    case "medic_death":
                        if ("1".equals(event.getUbercharge())) {
                            summary.incrementDrops();
                        }
                        break;
                }
            }

            // --- Handle pointcaptured/midfight ---
            if ("pointcaptured".equals(event.getEventType())) {
                Object capturingTeamObj = event.getExtras() != null ? event.getExtras().get("capturingTeam") : null;
                if (capturingTeamObj instanceof String) {
                    String capturingTeam = (String) capturingTeamObj;
                    if (teams.containsKey(capturingTeam)) {
                        teams.get(capturingTeam).incrementCaps();

                        if (waitingForMidfightCap && !midfightCaptured) {
                            teams.get(capturingTeam).incrementMidFights();
                            waitingForMidfightCap = false;
                            midfightCaptured = true;
                        }
                    }
                }
            }
        }


        return teams;
    }
}
