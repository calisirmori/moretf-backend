package com.moretf.model.module.chat;

import com.moretf.model.LogEvent;

import java.util.ArrayList;
import java.util.List;

public class ChatMessagesBuilder {
    public static List<ChatMessage> build(List<LogEvent> events) {
        List<ChatMessage> allMessages = new ArrayList<>();
        for (LogEvent event : events) {
            if(event.getEventType().equals("say") || event.getEventType().equals("say_team")){
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setPlayerId(event.getActor().getSteamId());
                chatMessage.setTime(event.getTimestamp());
                chatMessage.setMessage(event.getMessage());
                chatMessage.setTeamChat(event.getEventType().equals("say_team"));
                allMessages.add(chatMessage);
            }
        }
        return allMessages;
    }
}
