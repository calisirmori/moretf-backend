package com.moretf.model;

import com.moretf.model.module.chat.ChatMessage;
import com.moretf.model.module.fight.TeamFightSummary;
import com.moretf.model.module.match.MatchSummary;
import com.moretf.model.module.player.PlayerSummary;
import com.moretf.model.module.timeline.IntervalStat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchJsonResult {
    private MatchSummary info;
    private List<PlayerSummary> players;
    private List<LogEvent> events;
    private List<ChatMessage> chat;
    private List<LogEvent> killEvents;
    private List<IntervalStat> timeline;
    private List<TeamFightSummary> teamFights;
}
