package com.moretf.model.module.rounds;

import com.moretf.model.LogEvent;

import java.util.*;

public class RoundSummaryBuilder {

    public static List<RoundSummary> build(List<LogEvent> events) {
        List<RoundSummary> rounds = new ArrayList<>();
        RoundSummary current = null;

        long roundStart = 0;
        boolean inRound = false;

        for (LogEvent event : events) {
            switch (event.getEventType()) {
                case "round_start":
                    current = new RoundSummary();
                    current.setStartTime(event.getTimestamp() / 1000);
                    current.setTeam(new HashMap<>());
                    current.setPlayers(new HashMap<>());
                    roundStart = event.getTimestamp();
                    inRound = true;
                    break;

                case "round_win":
                    if (inRound && current != null) {
                        Object winner = event.getExtras() != null ? event.getExtras().get("winner") : null;
                        if (winner instanceof String) {
                            String winningTeam = (String) winner;
                            current.setWinner(winningTeam);
                            current.setLength((int) ((event.getTimestamp() - roundStart) / 1000));

                            current.getTeam()
                                    .computeIfAbsent(winningTeam, k -> new RoundTeamStats())
                                    .incrementScore();
                        }

                        rounds.add(current);
                        current = null;
                        inRound = false;
                    }
                    break;


                case "pointcaptured":
                    if (inRound && current != null && current.getFirstcap() == null) {
                        Object capturingTeam = event.getExtras() != null
                                ? event.getExtras().get("capturingTeam")
                                : null;
                        if (capturingTeam instanceof String) {
                            current.setFirstcap((String) capturingTeam);
                        }
                    }
                    break;
            }

            if (!inRound || current == null) continue;

            // Player and team stats
            String steamId = event.getActor() != null ? event.getActor().getSteamId() : null;
            String team = event.getActor() != null ? event.getActor().getTeam() : null;

            if (steamId != null && team != null) {
                RoundPlayerStats playerStats = current.getPlayers().computeIfAbsent(steamId, k -> new RoundPlayerStats(team));
                RoundTeamStats teamStats = current.getTeam().computeIfAbsent(team, k -> new RoundTeamStats());

                switch (event.getEventType()) {
                    case "kill":
                        playerStats.incrementKills();
                        teamStats.incrementKills();
                        break;

                    case "damage":
                        int damage = Math.min(event.getDamage(), 450);
                        playerStats.addDmg(damage);
                        teamStats.addDmg(damage);
                        break;

                    case "chargedeployed":
                        teamStats.incrementUbers();
                        break;
                }
            }
        }

        return rounds;
    }
}
