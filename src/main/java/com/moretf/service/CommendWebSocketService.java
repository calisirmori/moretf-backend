package com.moretf.service;

import com.moretf.dto.CommendBroadcast;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CommendWebSocketService {

    private final SimpMessagingTemplate messagingTemplate;

    public void broadcastCommend(CommendBroadcast payload) {
        String topic = "/topic/log/" + payload.getLogId() + "/commends";
        messagingTemplate.convertAndSend(topic, payload);
    }
}
