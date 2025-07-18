package com.moretf.model.module.playbyplay;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlayByPlayEvent {
    private long timestamp;
    private String clock;
    private String team;
    private String actingPlayerID;
    private String targetPlayerID;
    private Map<String, Integer> actingPlayerLocation;
    private Map<String, Integer> targetPlayerLocation;
    private String weapon;
    private String eventType;
    private String message;
    private Integer scoreRed;
    private Integer scoreBlue;
}
