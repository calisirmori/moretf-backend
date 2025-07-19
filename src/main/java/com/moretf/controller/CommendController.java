package com.moretf.controller;

import com.moretf.dto.CommendSummary;
import com.moretf.model.CommendEvent;
import com.moretf.service.CommendQueueService;
import com.moretf.service.CommendService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/log")
@RequiredArgsConstructor
public class CommendController {

    private final CommendQueueService queueService;
    private final CommendService commendService;

    @PostMapping("/{logId}/commend")
    public ResponseEntity<?> commendPlayer(@PathVariable String logId, @RequestBody CommendEvent event, @RequestHeader("X-User-ID") String commenderId) {
        event.setLogId(logId);
        event.setCommenderId(commenderId);
        queueService.sendCommend(event);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{logId}/commend/bulk")
    public ResponseEntity<?> commendMultiple(
            @PathVariable String logId,
            @RequestBody List<CommendEvent> events,
            @RequestHeader("X-User-ID") String commenderId
    ) {
        try {
            System.out.println("Bulk commend request received for logId: " + logId + " by " + commenderId);
            for (CommendEvent event : events) {
                System.out.println("Processing commend: " + event);
                event.setLogId(logId);
                event.setCommenderId(commenderId);
                queueService.sendCommend(event);
            }
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            e.printStackTrace(); // 🧨 This will show you the exact problem
            return ResponseEntity.status(500).body(Map.of("error", "Internal error occurred"));
        }
    }


    @GetMapping("/{logId}/commends")
    public ResponseEntity<Map<String, Object>> getCommendsForLog(
            @PathVariable String logId,
            @RequestHeader(value = "X-User-ID", required = false) String userId
    ) {
        List<CommendSummary> commends = commendService.getCommendSummaries(logId);
        List<String> youCommended = userId != null
                ? commendService.getCommendedByUser(logId, userId)
                : null;

        return ResponseEntity.ok(Map.of(
                "commends", commends,
                "youCommended", youCommended
        ));
    }
}
