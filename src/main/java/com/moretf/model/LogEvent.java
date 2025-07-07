package com.moretf.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class LogEvent {
    private Integer eventId;
    private long timestamp;
    private Actor actor;

    @JsonIgnore
    private String raw;

    private String eventType;
    private Target target;
    private String weapon;
    private Integer damage;
    private String message;
    private String stvLink;
    private String reason;
    private String assist;
    private String time;
    private Integer healing;
    private String item;
    private String newTeam;
    private String ubercharge;
    private String newName;
    private String character;
    private String address;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Map<String, Object> extras;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Actor {
        private String name;
        private String steamId;
        private String team;
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Target {
        private String name;
        private String steamId;
        private String team;
    }
}
