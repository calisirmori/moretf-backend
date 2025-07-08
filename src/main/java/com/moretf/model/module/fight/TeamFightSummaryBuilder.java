package com.moretf.model.module.fight;

import com.moretf.model.LogEvent;

import java.util.*;

public class TeamFightSummaryBuilder {

    public static List<TeamFightSummary> build(List<LogEvent> events) {
        if (events.isEmpty()) return List.of();

        List<LogEvent> relevantEvents = events.stream()
                .filter(e -> Set.of("kill", "damage").contains(e.getEventType()))
                .sorted(Comparator.comparingLong(LogEvent::getTimestamp))
                .toList();

        List<TeamFightSummary> fights = new ArrayList<>();
        long windowSize = 5000; // 5 seconds
        long stepSize = 1000; // 1 second
        long start = relevantEvents.get(0).getTimestamp();
        long end = relevantEvents.get(relevantEvents.size() - 1).getTimestamp();

        for (long loopStart = start; loopStart <= end; loopStart += stepSize) {
            final long windowStart = loopStart;
            final long windowEnd = windowStart + windowSize;

            List<LogEvent> windowEvents = relevantEvents.stream()
                    .filter(e -> e.getTimestamp() >= windowStart && e.getTimestamp() < windowEnd)
                    .toList();

            long killCount = windowEvents.stream().filter(e -> e.getEventType().equals("kill")).count();
            long damageSum = windowEvents.stream().filter(e -> e.getEventType().equals("damage") && e.getDamage() != null).mapToInt(LogEvent::getDamage).sum();

            if (killCount >= 2 || damageSum >= 1000) {
                TeamFightSummary fight = summarizeWindow(windowEvents, windowStart, windowEnd);
                if (!fights.isEmpty() && fight.start - fights.get(fights.size() - 1).end <= 1000) {
                    // merge with previous
                    TeamFightSummary last = fights.remove(fights.size() - 1);
                    last.end = fight.end;
                    last.playersInvolved.addAll(fight.playersInvolved);
                    last.blueStats.kills += fight.blueStats.kills;
                    last.blueStats.deaths += fight.blueStats.deaths;
                    last.blueStats.damageDealt += fight.blueStats.damageDealt;
                    last.blueStats.damageTaken += fight.blueStats.damageTaken;
                    last.redStats.kills += fight.redStats.kills;
                    last.redStats.deaths += fight.redStats.deaths;
                    last.redStats.damageDealt += fight.redStats.damageDealt;
                    last.redStats.damageTaken += fight.redStats.damageTaken;
                    fights.add(last);
                } else {
                    fights.add(fight);
                }
            }
        }

        return fights;
    }

    private static TeamFightSummary summarizeWindow(List<LogEvent> events, long start, long end) {
        TeamFightSummary summary = new TeamFightSummary();
        summary.start = start;
        summary.end = end;
        summary.playersInvolved = new HashSet<>();
        summary.blueStats = new TeamStats();
        summary.redStats = new TeamStats();

        for (LogEvent e : events) {
            String steamId = e.getActor() != null ? e.getActor().getSteamId() : null;
            String team = e.getActor() != null ? e.getActor().getTeam() : null;
            String targetTeam = e.getTarget() != null ? e.getTarget().getTeam() : null;

            if (steamId != null) summary.playersInvolved.add(steamId);
            if (e.getTarget() != null && e.getTarget().getSteamId() != null)
                summary.playersInvolved.add(e.getTarget().getSteamId());

            if ("damage".equals(e.getEventType()) && e.getDamage() != null && team != null) {
                if ("Blue".equals(team)) {
                    summary.blueStats.damageDealt += e.getDamage();
                    if ("Red".equals(targetTeam)) summary.redStats.damageTaken += e.getDamage();
                } else if ("Red".equals(team)) {
                    summary.redStats.damageDealt += e.getDamage();
                    if ("Blue".equals(targetTeam)) summary.blueStats.damageTaken += e.getDamage();
                }
            }

            if ("kill".equals(e.getEventType())) {
                if ("Blue".equals(team)) {
                    summary.blueStats.kills++;
                    if ("Red".equals(targetTeam)) summary.redStats.deaths++;
                } else if ("Red".equals(team)) {
                    summary.redStats.kills++;
                    if ("Blue".equals(targetTeam)) summary.blueStats.deaths++;
                }
            }
        }

        return summary;
    }
}
