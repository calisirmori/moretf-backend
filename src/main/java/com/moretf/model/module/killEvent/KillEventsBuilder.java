package com.moretf.model.module.killEvent;

import com.moretf.model.LogEvent;

import java.util.List;

public class KillEventsBuilder {
    public static List<LogEvent> build(List<LogEvent> events) {
        return events.stream()
                .filter(e -> "kill".equalsIgnoreCase(e.getEventType()))
                .toList();
    }
}