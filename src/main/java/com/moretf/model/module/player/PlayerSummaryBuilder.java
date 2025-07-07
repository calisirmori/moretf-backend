package com.moretf.model.module.player;

import com.moretf.model.LogEvent;
import java.util.*;

public class PlayerSummaryBuilder {
    public static List<PlayerSummary> build(List<LogEvent> events) {
        Map<String, PlayerSummary> players = new HashMap<>();

        for (LogEvent event : events) {
            if (event.getActor() == null || event.getActor().getSteamId() == null) continue;
            String steamId = event.getActor().getSteamId();
            players.putIfAbsent(steamId,
                    new PlayerSummary(
                            event.getActor().getName(),
                            steamId,
                            event.getActor().getTeam()
                    )
            );

            if ("kill".equals(event.getEventType())) {
                players.get(steamId).incrementKills();
            }
        }

        return new ArrayList<>(players.values());
    }
}
