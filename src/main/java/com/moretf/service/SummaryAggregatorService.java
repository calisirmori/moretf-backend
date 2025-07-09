package com.moretf.service;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
import com.moretf.model.module.chat.ChatMessage;
import com.moretf.model.module.chat.ChatMessagesBuilder;
import com.moretf.model.module.fight.TeamFightSummary;
import com.moretf.model.module.fight.TeamFightSummaryBuilder;
import com.moretf.model.module.killEvent.KillEventsBuilder;
import com.moretf.model.module.match.MatchSummary;
import com.moretf.model.module.match.MatchSummaryBuilder;
import com.moretf.model.module.player.PlayerSummary;
import com.moretf.model.module.player.PlayerSummaryBuilder;
import com.moretf.model.module.timeline.IntervalStat;
import com.moretf.model.module.timeline.TimelineBuilder;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SummaryAggregatorService {

    private final MatchSummaryBuilder matchSummaryBuilder;

    public MatchJsonResult buildMatchJson(List<LogEvent> events, int logId, String title, String map) {
        List<PlayerSummary> players = PlayerSummaryBuilder.build(events);
        MatchSummary matchSummary = matchSummaryBuilder.build(events, logId, title, map);
        List<ChatMessage> chatMessages = ChatMessagesBuilder.build(events);
        List<LogEvent> killEvents = KillEventsBuilder.build(events);
        List<IntervalStat> timeline = TimelineBuilder.build(events);
        List<TeamFightSummary> teamFights = TeamFightSummaryBuilder.build(events);
        return new MatchJsonResult(matchSummary, players, events, chatMessages, killEvents, timeline, teamFights);
    }

    public MatchJsonResult buildMatchJsonWithoutEvents(List<LogEvent> events, int logId, String title, String map) {
        List<PlayerSummary> players = PlayerSummaryBuilder.build(events);
        MatchSummary matchSummary = matchSummaryBuilder.build(events, logId, title, map);
        List<ChatMessage> chatMessages = ChatMessagesBuilder.build(events);
        List<LogEvent> killEvents = KillEventsBuilder.build(events);
        List<IntervalStat> timeline = TimelineBuilder.build(events);
        List<TeamFightSummary> teamFights = TeamFightSummaryBuilder.build(events);

        // Empty event list
        return new MatchJsonResult(matchSummary, players, List.of(), chatMessages, killEvents, timeline, teamFights);
    }
}
