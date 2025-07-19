package com.moretf.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.moretf.model.CommendEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

@Service
@RequiredArgsConstructor
public class CommendQueueService {

    private final SqsClient sqsClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final String QUEUE_URL = "https://sqs.us-east-2.amazonaws.com/742846646501/moretf-commend-queue"; // Replace if needed

    public void sendCommend(CommendEvent event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);

            SendMessageRequest sendMsgRequest = SendMessageRequest.builder()
                    .queueUrl(QUEUE_URL)
                    .messageBody(messageBody)
                    .build();

            sqsClient.sendMessage(sendMsgRequest);
        } catch (Exception e) {
            e.printStackTrace(); // or log properly
        }
    }
}
