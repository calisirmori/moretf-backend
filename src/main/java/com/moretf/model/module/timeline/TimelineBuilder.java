package com.moretf.model.module.timeline;

import com.moretf.model.LogEvent;

import java.util.*;

public class TimelineBuilder {

    public static List<IntervalStat> build(List<LogEvent> events) {
        if (events.isEmpty()) return List.of();

        long matchStart = events.get(0).getTimestamp();
        Map<Integer, Map<String, PlayerAggregate>> buckets = new TreeMap<>();

        for (LogEvent event : events) {
            long delta = event.getTimestamp() - matchStart;
            int bucket = (int) (delta / 10000);

            String actorId = event.getActor() != null ? event.getActor().getSteamId() : null;
            String actorTeam = event.getActor() != null ? event.getActor().getTeam() : "Unknown";

            Map<String, PlayerAggregate> bucketMap = buckets.computeIfAbsent(bucket, b -> new HashMap<>());

            // DAMAGE
            if ("damage".equals(event.getEventType()) && event.getDamage() != null && event.getDamage() > 0 && actorId != null) {
                PlayerAggregate agg = bucketMap.getOrDefault(actorId, new PlayerAggregate(actorTeam));
                agg.damage += event.getDamage();
                bucketMap.put(actorId, agg);
            }

            // HEALING
            if ("healed".equals(event.getEventType()) && event.getHealing() != null && event.getHealing() > 0 && actorId != null) {
                PlayerAggregate agg = bucketMap.getOrDefault(actorId, new PlayerAggregate(actorTeam));
                agg.healing += event.getHealing();
                bucketMap.put(actorId, agg);
            }

            // KILL + DEATH
            if ("kill".equals(event.getEventType())) {
                // Actor = killer
                if (actorId != null) {
                    PlayerAggregate killer = bucketMap.getOrDefault(actorId, new PlayerAggregate(actorTeam));
                    killer.kills += 1;
                    bucketMap.put(actorId, killer);
                }

                // Target = victim
                if (event.getTarget() != null) {
                    String victimId = event.getTarget().getSteamId();
                    String victimTeam = event.getTarget().getTeam();
                    if (victimId != null) {
                        PlayerAggregate victim = bucketMap.getOrDefault(victimId, new PlayerAggregate(victimTeam));
                        victim.deaths += 1;
                        bucketMap.put(victimId, victim);
                    }
                }
            }

            // UBER (CHARGE DEPLOYED)
            if ("charge_deployed".equals(event.getEventType()) && actorId != null) {
                PlayerAggregate agg = bucketMap.getOrDefault(actorId, new PlayerAggregate(actorTeam));
                agg.ubers += 1;
                bucketMap.put(actorId, agg);
            }
        }

        List<IntervalStat> result = new ArrayList<>();
        for (Map.Entry<Integer, Map<String, PlayerAggregate>> entry : buckets.entrySet()) {
            int bucket = entry.getKey();
            long intervalStart = matchStart + bucket * 10000L;
            long intervalEnd = intervalStart + 10000L;
            result.add(new IntervalStat(intervalStart, intervalEnd, entry.getValue()));
        }

        return result;
    }
}
