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

                case "round_stalemate":
                    if (roundStart != null) {
                        int roundLength = (int) ((event.getTimestamp() - roundStart - roundPauseTime) / 1000);
                        RoundInfo roundInfo = new RoundInfo(roundLength, "Stalemate");

                        totalMatchDuration += roundLength;
                        rounds.put(roundNumber++, roundInfo);
                    }
                    roundStart = null;
                    roundPauseTime = 0;
                    break;

                case "round_win":
                    String winner = event.getExtras() != null ? event.getExtras().get("winner").toString() : null;
                    if (winner != null) {
                        if (winner.equalsIgnoreCase("Red")) redScore++;
                        else bluScore++;

                        if (roundStart != null) {
                            int roundLength = (int) ((event.getTimestamp() - roundStart - roundPauseTime) / 1000);
                            RoundInfo roundInfo = new RoundInfo(roundLength, winner);

                            totalMatchDuration += roundLength;
                            rounds.put(roundNumber++, roundInfo);
                        }

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
        // Determine winner from score
        String matchWinner = bluScore > redScore ? "Blue" : redScore > bluScore ? "Red" : "Tie";

        // Step 1: Try from RDS logs table
        LogSummary fetchedSummary = null;
        if ((title == null || title.isBlank()) || (map == null || map.isBlank())) {
            try {
                Optional<LogSummary> optional = logSummaryRepository.findById((long) logId);
                if (optional.isPresent()) {
                    fetchedSummary = optional.get();
                    if ((title == null || title.isBlank()) && fetchedSummary.getTitle() != null) {
                        title = fetchedSummary.getTitle();
                    }
                    if ((map == null || map.isBlank()) && fetchedSummary.getMap() != null) {
                        map = fetchedSummary.getMap();
                    }
                }
            } catch (Exception e) {
                System.err.println("Warning: failed to fetch title/map from DB for logId " + logId);
            }
        }

        String summaryMap = map != null ? map : (fetchedSummary != null ? fetchedSummary.getMap() : null);

        if (summaryMap != null && (summaryMap.startsWith("pl_") || summaryMap.startsWith("cp_steel")) && rounds.size() == 2) {
            if (redScore == 0 && bluScore == 2) {
                matchWinner = "Blue";
            } else if (redScore == 2 && bluScore == 0) {
                matchWinner = "Red";
            } else if (redScore == 1 && bluScore == 1) {
                RoundInfo firstRound = rounds.get(1);
                if ("Blue".equalsIgnoreCase(firstRound.getWinner())) {
                    matchWinner = "Red";
                } else if ("Red".equalsIgnoreCase(firstRound.getWinner())) {
                    matchWinner = "Blue";
                } else {
                    matchWinner = "Tie";
                }
            } else {
                matchWinner = "Tie";
            }
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

        return new MatchSummary(
                logId,
                title,
                map,
                matchStartTime,
                matchWinner,
                redScore,
                bluScore,
                totalMatchDuration,
                gameOverEvents == 1 ? "false" : gameOverEvents == 0 ? "unknown" : "true",
                rounds
        );
    }

}
