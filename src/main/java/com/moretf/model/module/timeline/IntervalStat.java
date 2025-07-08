package com.moretf.model.module.timeline;

import java.util.Map;

public class IntervalStat {
    public long intervalStart;
    public long intervalEnd;
    public Map<String, PlayerAggregate> players;

    public IntervalStat(long start, long end, Map<String, PlayerAggregate> players) {
        this.intervalStart = start;
        this.intervalEnd = end;
        this.players = players;
    }
}