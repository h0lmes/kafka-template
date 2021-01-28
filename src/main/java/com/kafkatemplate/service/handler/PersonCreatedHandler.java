package com.kafkatemplate.service.handler;

import java.time.LocalDateTime;

import com.kafkatemplate.model.message.PersonCreated;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.kafkatemplate.enums.Status;
import com.kafkatemplate.model.message.IdentityLinked;
import com.kafkatemplate.service.KafkaProducer;

@Service
public class PersonCreatedHandler {

    private final Logger log = LoggerFactory.getLogger(PersonCreatedHandler.class);
    private final KafkaProducer producer;

    public PersonCreatedHandler(KafkaProducer producer) {
        this.producer = producer;
    }

    public void consumeMessage(PersonCreated pcMessage) {
        log.debug("Start handling message: {}", pcMessage);
        IdentityLinked msg = new IdentityLinked();
        msg.setTraceId(pcMessage.getTraceId());
        msg.setEmittedAt(LocalDateTime.now());
        msg.setStatus(Status.OK);
        msg.setPayload(pcMessage.getPayload());
        producer.sendMessage(msg);
        log.debug("Finish handling message: {}", pcMessage);
    }
}