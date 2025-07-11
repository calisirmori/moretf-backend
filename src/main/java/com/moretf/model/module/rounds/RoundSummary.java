package com.moretf.model.module.rounds;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
public class RoundSummary {
    private long startTime;
    private String winner;
    private String firstcap;
    private int length;

    private Map<String, RoundTeamStats> team;
    private Map<String, RoundPlayerStats> players;

    // Getters and setters
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }

    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }

    public String getFirstcap() { return firstcap; }
    public void setFirstcap(String firstcap) { this.firstcap = firstcap; }

    public int getLength() { return length; }
    public void setLength(int length) { this.length = length; }

    public Map<String, RoundTeamStats> getTeam() { return team; }
    public void setTeam(Map<String, RoundTeamStats> team) { this.team = team; }

    public Map<String, RoundPlayerStats> getPlayers() { return players; }
    public void setPlayers(Map<String, RoundPlayerStats> players) { this.players = players; }

}
