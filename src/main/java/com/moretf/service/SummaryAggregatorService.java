package com.moretf.service;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
import com.moretf.model.PlayerSummaryEntity;
import com.moretf.model.module.chat.ChatMessage;
import com.moretf.model.module.chat.ChatMessagesBuilder;
import com.moretf.model.module.fight.TeamFightSummary;
import com.moretf.model.module.fight.TeamFightSummaryBuilder;
import com.moretf.model.module.match.MatchSummary;
import com.moretf.model.module.match.MatchSummaryBuilder;
import com.moretf.model.module.playbyplay.PlayByPlayBuilder;
import com.moretf.model.module.playbyplay.PlayByPlayEvent;
import com.moretf.model.module.player.PlayerSummary;
import com.moretf.model.module.player.PlayerSummaryBuilder;
import com.moretf.model.module.rounds.RoundSummary;
import com.moretf.model.module.rounds.RoundSummaryBuilder;
import com.moretf.model.module.teams.TeamSummary;
import com.moretf.model.module.teams.TeamSummaryBuilder;
import com.moretf.model.module.timeline.IntervalStat;
import com.moretf.model.module.timeline.TimelineBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryAggregatorService {

    private final MatchSummaryBuilder matchSummaryBuilder;

    public MatchJsonResult buildMatchJson(List<LogEvent> events, int logId, String title, String map) {
        List<PlayerSummary> players = PlayerSummaryBuilder.build(events);
        MatchSummary matchSummary = matchSummaryBuilder.build(events, logId, title, map);
        List<ChatMessage> chatMessages = ChatMessagesBuilder.build(events);
        List<IntervalStat> timeline = TimelineBuilder.build(events);
        List<TeamFightSummary> teamFights = TeamFightSummaryBuilder.build(events);
        Map<String, TeamSummary> teamStats = TeamSummaryBuilder.build(events);
        List<RoundSummary> rounds = RoundSummaryBuilder.build(events);
        List<PlayByPlayEvent> playByPlay = PlayByPlayBuilder.build(events);
        return new MatchJsonResult(matchSummary, players, events, chatMessages, timeline, teamFights, teamStats, rounds, playByPlay);
    }

    public MatchJsonResult buildMatchJsonForTest(List<LogEvent> events, int logId, String title, String map) {
        List<PlayerSummary> players = PlayerSummaryBuilder.build(events);
        MatchSummary matchSummary = matchSummaryBuilder.build(events, logId, title, map);
//        List<ChatMessage> chatMessages = ChatMessagesBuilder.build(events);
//        List<IntervalStat> timeline = TimelineBuilder.build(events);
//        List<TeamFightSummary> teamFights = TeamFightSummaryBuilder.build(events);
        Map<String, TeamSummary> teamStats = TeamSummaryBuilder.build(events);
        List<RoundSummary> rounds = RoundSummaryBuilder.build(events);
//        List<PlayByPlayEvent> playByPlay = PlayByPlayBuilder.build(events);
        return new MatchJsonResult(matchSummary, players, List.of(), List.of(), List.of(), List.of(), teamStats, rounds, List.of());
    }

    public MatchJsonResult buildMatchJsonWithoutEvents(List<LogEvent> events, int logId, String title, String map) {
        List<PlayerSummary> players = PlayerSummaryBuilder.build(events);
        MatchSummary matchSummary = matchSummaryBuilder.build(events, logId, title, map);
        List<ChatMessage> chatMessages = ChatMessagesBuilder.build(events);
        List<IntervalStat> timeline = TimelineBuilder.build(events);
        List<TeamFightSummary> teamFights = TeamFightSummaryBuilder.build(events);
        Map<String, TeamSummary> teamStats = TeamSummaryBuilder.build(events);
        List<RoundSummary> rounds = RoundSummaryBuilder.build(events);
        List<PlayByPlayEvent> playByPlay = PlayByPlayBuilder.build(events);
        // Empty event list
        return new MatchJsonResult(matchSummary, players, List.of(), chatMessages, timeline, teamFights, teamStats, rounds, playByPlay);
    }

    public List<PlayerSummaryEntity> buildPlayerSummaries(List<LogEvent> events, int logId) {
        List<PlayerSummary> summaries = PlayerSummaryBuilder.build(events);
        MatchSummary match = matchSummaryBuilder.build(events, logId, null, null);// for duration

        int durationSeconds = match.getDurationSeconds();
        String winner = match.getWinner();

        return summaries.stream().map(p -> {
            long steamId64 = 76561197960265728L + Long.parseLong(
                    p.getSteamId().replace("[U:1:", "").replace("]", "")
            );
            int dm = p.getDamage();
            int dpm = durationSeconds > 0 ? Math.round(dm / (durationSeconds / 60f)) : 0;
            int dt = p.getDamageTaken();
            int dtm = durationSeconds > 0 ? Math.round(dt / (durationSeconds / 60f)) : 0;
            if ( p.getTeam() == null ) System.out.println();
            return new PlayerSummaryEntity(
                    p.getKills(),
                    p.getAssists(),
                    p.getDeaths(),
                    dpm,
                    dm,
                    dtm,
                    dt,
                    p.getHealing(),
                    p.getTotalTime(),
                    p.getCharacter().toLowerCase(),
                    p.getTeam(),
                    p.getName(),
                    p.getTeam().equals(winner) ? "W" : "Tie".equals(winner) ? "T" : "L",
                    steamId64,
                    logId
            );

        }).collect(Collectors.toList());
    }


}
