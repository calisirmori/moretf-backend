package com.moretf.model.module.match;

import com.fasterxml.jackson.databind.JsonNode;
import com.moretf.client.LogsTfApiClient;
import com.moretf.model.LogEvent;

import java.util.*;

public class MatchSummaryBuilder {

    public static MatchSummary build(List<LogEvent> events, int logId) {
        Map<Integer, RoundInfo> rounds = new LinkedHashMap<>();

        int gameOverEvents = 0;

        int redScore = 0;
        int bluScore = 0;

        int roundNumber = 1;
        int roundPauseTime = 0;
        Long roundStart = null;

        Long matchStartTime = null;

        int totalMatchDuration = 0;

        for (LogEvent event : events) {
            switch (event.getEventType()) {
                case "round_start":
                    roundStart = event.getTimestamp();
                    if(matchStartTime == null) matchStartTime = roundStart;
                    break;

                case "round_win":
                    String winner = event.getExtras() != null ? event.getExtras().get("winner").toString() : null;
                    if (winner != null && roundStart != null) {
                        if (winner.equalsIgnoreCase("Red")) redScore++;
                        else bluScore++;

                        int roundLength = (int) ((event.getTimestamp() - roundStart - roundPauseTime) / 1000);

                        RoundInfo roundInfo = new RoundInfo(roundLength, winner);

                        //Match Duration
                        totalMatchDuration += (int) roundLength;

                        //Rounds
                        rounds.put(roundNumber++, roundInfo);

                        roundStart = null;
                        roundPauseTime = 0;
                    }
                    break;

                case "game_over":
                    gameOverEvents++;
                    break;

                case "pause_length":
                    if (event.getExtras() != null && event.getExtras().get("seconds") != null) {
                        roundPauseTime = (int) Double.parseDouble(event.getExtras().get("seconds").toString()) * 1000;
                    }
                    break;
            }
        }

        String matchWinner = bluScore > redScore ? "Blu" : redScore > bluScore ? "Red" : "Tie";

        JsonNode info = LogsTfApiClient.fetchLogInfo(logId);
        String title = info.get("title").asText();
        String map = info.get("map").asText();

        return new MatchSummary(
                logId,
                title,
                map,
                matchStartTime,
                matchWinner,
                redScore,
                bluScore,
                totalMatchDuration,
                gameOverEvents > 1,
                rounds
        );
    }

}
