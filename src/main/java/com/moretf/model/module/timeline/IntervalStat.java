package com.moretf.model.module.timeline;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IntervalStat {
    public long intervalStart;
    public long intervalEnd;
    public Map<String, PlayerAggregate> players;
}
