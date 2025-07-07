package com.moretf.service;

import com.moretf.model.LogEvent;
import com.moretf.model.MatchJsonResult;
import com.moretf.model.module.chat.ChatMessage;
import com.moretf.model.module.chat.ChatMessagesBuilder;
import com.moretf.model.module.match.MatchSummary;
import com.moretf.model.module.match.MatchSummaryBuilder;
import com.moretf.model.module.player.PlayerSummary;
import com.moretf.model.module.player.PlayerSummaryBuilder;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class SummaryAggregatorService {

    public MatchJsonResult buildMatchJson(List<LogEvent> events, int id) {
        List<PlayerSummary> players = PlayerSummaryBuilder.build(events);
        MatchSummary matchSummary = MatchSummaryBuilder.build(events, id);
        List<ChatMessage> chatMessages = ChatMessagesBuilder.build(events);
        return new MatchJsonResult(matchSummary, players, events, chatMessages);
    }
}
