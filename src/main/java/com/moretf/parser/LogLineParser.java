package com.moretf.parser;

import com.moretf.model.LogEvent;

public interface LogLineParser {
    boolean matches(String line);
    LogEvent parse(String line, int eventId);
}