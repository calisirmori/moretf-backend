package com.moretf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretf.model.CommendEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.*;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommendQueueListener {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper;
    private final CommendService commendService; // You handle DB logic here

    private final String QUEUE_URL = "https://sqs.us-east-2.amazonaws.com/742846646501/moretf-commend-queue";

    @Scheduled(fixedDelay = 5000)
    public void pollCommendQueue() {
        try {
            ReceiveMessageRequest request = ReceiveMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .maxNumberOfMessages(5)
                    .waitTimeSeconds(2)
                    .build();

            List<Message> messages = sqsClient.receiveMessage(request).messages();

            for (Message msg : messages) {
                CommendEvent event = objectMapper.readValue(msg.body(), CommendEvent.class);

                log.info("Received commend event: {}", event);

                commendService.processCommend(event); // Your logic: update DB, check dedupe, etc.

                // Delete from queue
                sqsClient.deleteMessage(DeleteMessageRequest.builder()
                        .queueUrl(QUEUE_URL)
                        .receiptHandle(msg.receiptHandle())
                        .build());
            }

        } catch (Exception e) {
            log.error("Error polling commend queue", e);
        }
    }
}
