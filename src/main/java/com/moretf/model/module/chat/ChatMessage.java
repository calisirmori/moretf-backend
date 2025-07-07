package com.moretf.model.module.chat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private String playerId;
    private Long time;
    private String message;
    private boolean isTeamChat;
}
