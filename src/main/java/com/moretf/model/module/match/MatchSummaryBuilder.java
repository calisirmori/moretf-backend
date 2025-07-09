package com.moretf.model.module.match;

import com.fasterxml.jackson.databind.JsonNode;
import com.moretf.LogMetaData.LogSummary;
import com.moretf.client.LogsTfApiClient;
import com.moretf.model.LogEvent;
import com.moretf.repository.LogSummaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class MatchSummaryBuilder {

    private final LogSummaryRepository logSummaryRepository;

    public MatchSummary build(List<LogEvent> events, int logId, String title, String map) {

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
                    roundPauseTime = 0;
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

        // Step 1: Try from RDS logs table
        if (title == null || title.isBlank() || map == null || map.isBlank()) {
            try {
                Optional<LogSummary> optional = logSummaryRepository.findById((long) logId);
                if (optional.isPresent()) {
                    LogSummary summary = optional.get();
                    if ((title == null || title.isBlank()) && summary.getTitle() != null) {
                        title = summary.getTitle();
                    }
                    if ((map == null || map.isBlank()) && summary.getMap() != null) {
                        map = summary.getMap();
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: failed to fetch title/map from DB for logId " + logId);
            }

//            // Step 2: Fallback to Logs.tf API
//            if (title == null || title.isBlank() || map == null || map.isBlank()) {
//                JsonNode info = LogsTfApiClient.fetchLogInfo(logId);
//                if (title == null || title.isBlank()) {
//                    title = info.get("title").asText();
//                }
//                if (map == null || map.isBlank()) {
//                    map = info.get("map").asText();
//                }
//            }
        }


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
