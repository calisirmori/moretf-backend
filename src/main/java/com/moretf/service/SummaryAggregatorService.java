package com.moretf.service;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
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
}
