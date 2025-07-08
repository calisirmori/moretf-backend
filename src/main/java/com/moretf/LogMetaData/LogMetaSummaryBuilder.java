package com.moretf.LogMetaData;

import com.moretf.model.LogEvent;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LogMetaSummaryBuilder {

    public static LogSummary extractMeta(long logId, List<LogEvent> events, String titleOverride, String mapOverride) {
        LogSummary summary = new LogSummary();
        summary.setLogid(logId);

        // Title from POST param or fallback
        summary.setTitle(titleOverride != null ? titleOverride : "Match " + logId);

        // Pause-aware match duration
        int totalDuration = 0;
        int roundPauseTime = 0;
        Long roundStart = null;

        for (LogEvent event : events) {
            switch (event.getEventType()) {
                case "round_start":
                    roundStart = event.getTimestamp();
                    roundPauseTime = 0;
                    break;

                case "round_win":
                    if (roundStart != null) {
                        long roundEnd = event.getTimestamp();
                        totalDuration += (int) ((roundEnd - roundStart - roundPauseTime) / 1000);
                        roundStart = null;
                        roundPauseTime = 0;
                    }
                    break;

                case "pause_length":
                    if (event.getExtras() != null && event.getExtras().get("seconds") != null) {
                        roundPauseTime = (int) Double.parseDouble(event.getExtras().get("seconds").toString()) * 1000;
                    }
                    break;
            }
        }
        summary.setMatchLength(totalDuration);

        // Scores
        int red = 0;
        int blu = 0;
        for (LogEvent event : events) {
            if ("round_win".equals(event.getEventType())) {
                String winner = event.getExtras().getOrDefault("winner", "").toString();
                if ("Red".equalsIgnoreCase(winner)) red++;
                else if ("Blue".equalsIgnoreCase(winner)) blu++;
            }
        }
        summary.setRedscore((short) red);
        summary.setBluescore((short) blu);

        // Unique players
        Set<String> steamIds = new HashSet<>();
        for (LogEvent e : events) {
            if (e.getActor() != null) steamIds.add(e.getActor().getSteamId());
        }
        summary.setPlayers(steamIds.size());

        // Map name from POST param or fallback
        summary.setMap(mapOverride != null ? mapOverride : "unknown");

        // Date
        long logStart = events.get(0).getTimestamp();
        summary.setLogDate(logStart);

        // Format
        summary.setFormat(steamIds.size() <= 12 ? "6v6" : "Highlander");

        // Game type
        summary.setGameType(null);

        // Combined if more than 1 game_over event
        long gameOverCount = events.stream().filter(e -> "game_over".equals(e.getEventType())).count();
        summary.setCombined(String.valueOf(gameOverCount > 1));

        return summary;
    }
}
