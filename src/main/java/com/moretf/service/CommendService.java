package com.moretf.service;

import com.moretf.dto.CommendBroadcast;
import com.moretf.dto.CommendSummary;
import com.moretf.model.CommendEvent;
import com.moretf.repository.LogCommendRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommendService {

    private final LogCommendRepository logCommendRepository;
    private final CommendWebSocketService webSocketService;

    public void processCommend(CommendEvent event) {
        log.info("Saving commend from {} to {} in log {}", event.getCommenderId(), event.getCommendedId(), event.getLogId());
        logCommendRepository.saveCommend(event);

        int count = logCommendRepository
                .getCommendSummaryForLog(event.getLogId())
                .stream()
                .filter(c -> c.getSteamId().equals(event.getCommendedId()))
                .findFirst()
                .map(c -> c.getCount())
                .orElse(1); // fallback

        CommendBroadcast payload = new CommendBroadcast();
        payload.setLogId(event.getLogId());
        payload.setCommenderId(event.getCommenderId());
        payload.setCommendedId(event.getCommendedId());
        payload.setTotalCount(count);

        webSocketService.broadcastCommend(payload);
    }

    public List<CommendSummary> getCommendSummaries(String logId) {
        return logCommendRepository.getCommendSummaryForLog(logId);
    }

    public List<String> getCommendedByUser(String logId, String userId) {
        return logCommendRepository.getCommendedIdsByUser(logId, userId);
    }
}
