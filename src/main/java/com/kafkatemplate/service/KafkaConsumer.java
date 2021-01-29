package com.kafkatemplate.service;

import java.util.stream.Collectors;

import com.kafkatemplate.model.message.PersonCreated;
import com.kafkatemplate.model.message.StatusCheck;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.stereotype.Service;
import com.kafkatemplate.service.handler.PersonCreatedHandler;

@Service
@KafkaListener(topics = "#{'${spring.kafka.consumer.topic}'}")
public class KafkaConsumer {

    private final Logger log = LoggerFactory.getLogger(KafkaConsumer.class);
    private final PersonCreatedHandler personCreatedHandler;

    public KafkaConsumer(PersonCreatedHandler personCreatedHandler) {
        this.personCreatedHandler = personCreatedHandler;
    }
    
    @KafkaHandler
    public void listen(PersonCreated message, @Headers MessageHeaders headers, Acknowledgment acknowledgment) {
        log.info("Got Message: {}\nHeaders: {}", message, headersToString(headers));
        personCreatedHandler.consumeMessage(message);
        acknowledgment.acknowledge();
    }
    
    @KafkaHandler
    public void listen(StatusCheck message, @Headers MessageHeaders headers, Acknowledgment acknowledgment) {
        log.debug("Got status check message: {}\nHeaders: {}", message, headersToString(headers));
        acknowledgment.acknowledge();
    }

    private String headersToString(MessageHeaders headers) {
        return headers.entrySet().stream()
            .map(e -> e.getKey() + "=" + e.getValue())
            .collect(Collectors.joining(", ", "{", "}"));
    }
}