package com.kafkatemplate.service;

import com.kafkatemplate.model.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

@Service
public class KafkaProducer {

    @Value("${spring.kafka.producer.topic}")
    private String topic;

    private final Logger log = LoggerFactory.getLogger(KafkaProducer.class);
    private final KafkaTemplate<String, Message> kafkaTemplate;

    public KafkaProducer(KafkaTemplate<String, Message> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendMessage(Message message) {
        log.debug("Producing message: {}", message);
        
        ListenableFuture<SendResult<String, Message>> future = kafkaTemplate.send(topic, message);
        future.addCallback(new ListenableFutureCallback<SendResult<String, Message>>() {

            @Override
            public void onFailure(Throwable e) {
                log.error("Unable to send message=[ {} ] ", message, e);
            }

            @Override
            public void onSuccess(SendResult<String, Message> result) {
                log.info("Sent message=[ {} ] with offset=[ {} ]", message, result.getRecordMetadata().offset());
            }
        });
    }
}