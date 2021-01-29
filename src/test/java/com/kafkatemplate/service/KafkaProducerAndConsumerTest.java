package com.kafkatemplate.service;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.KafkaMessageListenerContainer;
import org.springframework.kafka.listener.MessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.kafka.test.rule.EmbeddedKafkaRule;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.messaging.MessageHeaders;
import org.springframework.test.context.junit4.SpringRunner;
import com.kafkatemplate.enums.Status;
import com.kafkatemplate.model.message.IdentityLinked;
import com.kafkatemplate.model.message.Message;
import com.kafkatemplate.model.message.PersonCreated;
import com.kafkatemplate.model.payload.Payload;
import com.kafkatemplate.service.handler.PersonCreatedHandler;

@TestInstance(PER_CLASS)
@RunWith(SpringRunner.class)
@SpringBootTest
public class KafkaProducerAndConsumerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaProducerAndConsumerTest.class);
    private static final String TOPIC = "test-topic";
    private KafkaMessageListenerContainer<String, Message> container;
    private BlockingQueue<ConsumerRecord<String, Message>> consumerRecords;
    @ClassRule
    public static EmbeddedKafkaRule embeddedKafka = new EmbeddedKafkaRule(1, true, TOPIC);
    private final ObjectMapper mapper = new ObjectMapper();
    @SpyBean
    private KafkaProducer kafkaProducer;
    @SpyBean
    private KafkaConsumer kafkaConsumer;
    @SpyBean
    private PersonCreatedHandler handler;
    
    @BeforeClass
    public static void setProps() {
        System.setProperty("spring.kafka.listener.ack-mode", "manual_immediate");
        System.setProperty("spring.kafka.bootstrap-servers", "${spring.embedded.kafka.brokers}");
        
        System.setProperty("spring.kafka.producer.topic", TOPIC);
        System.setProperty("spring.kafka.producer.key-serializer", "org.apache.kafka.common.serialization.StringSerializer");
        System.setProperty("spring.kafka.producer.value-serializer", "org.springframework.kafka.support.serializer.JsonSerializer");
        
        System.setProperty("spring.kafka.consumer.group-id", "gr-1");
        System.setProperty("spring.kafka.consumer.topic", TOPIC);
        System.setProperty("spring.kafka.consumer.enable-auto-commit", "false");
        System.setProperty("spring.kafka.consumer.auto-offset-reset", "earliest");
        System.setProperty("spring.kafka.consumer.key-deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        System.setProperty("spring.kafka.consumer.value-deserializer", "org.springframework.kafka.support.serializer.JsonDeserializer");
        System.setProperty("spring.kafka.consumer.properties.spring.json.trusted.packages", "com.kafkatemplate.model.message");
    }
    
    @Before
    public void setUp() {
        consumerRecords = new LinkedBlockingQueue<>();
        ContainerProperties containerProperties = new ContainerProperties(TOPIC);
        Map<String, Object> consumerProperties = KafkaTestUtils.consumerProps("sender", "false", embeddedKafka.getEmbeddedKafka());
        DefaultKafkaConsumerFactory<String, Message> consumer = new DefaultKafkaConsumerFactory<>(consumerProperties);
        container = new KafkaMessageListenerContainer<>(consumer, containerProperties);
        container.setupMessageListener((MessageListener<String, Message>) record -> {
            LOGGER.debug("Listened message='{}'", record.toString());
            consumerRecords.add(record);
        });
        container.start();
        ContainerTestUtils.waitForAssignment(container, embeddedKafka.getEmbeddedKafka().getPartitionsPerTopic());
    }
    
    @After
    public void tearDown() {
        container.stop();
    }
    
    @Test
    public void producerAndConsumerTestWithPayload() throws InterruptedException, JsonProcessingException {
        String expectedMessage = "{\"event\":\"created\",\"status\":\"ok\","
                + "\"payload\":{\"id\":\"11111111-1111-4000-1111-111111111111\",\"value\":\"22222222-2222-4000-2222-222222222222\"},"
                + "\"emitted_at\":\"2021-01-29T12:00:00.000Z\",\"trace_id\":\"00000000-0000-4000-0000-000000000000\"}";
        UUID traceId = UUID.fromString("00000000-0000-4000-0000-000000000000");
        UUID value = UUID.fromString("22222222-2222-4000-2222-222222222222");
        UUID id = UUID.fromString("11111111-1111-4000-1111-111111111111");
        
        Payload payloadSuccess = new Payload();
        payloadSuccess.setValue(value);
        payloadSuccess.setId(id);
        
        PersonCreated event = new PersonCreated();
        event.setStatus(Status.OK);
        event.setEmittedAt(LocalDateTime.of(2021, 1, 29, 12, 0, 0, 0));
        event.setTraceId(traceId);
        event.setPayload(payloadSuccess);
        
        doNothing().when(handler).consumeMessage(any());
        
        kafkaProducer.sendMessage(event);
        
        ConsumerRecord<String, Message> received = consumerRecords.poll(10, TimeUnit.SECONDS);
        Object receivedValue = Objects.requireNonNull(received).value();
        PersonCreated result = mapper.readValue(receivedValue.toString(), PersonCreated.class);
        
        assertEquals(expectedMessage, receivedValue);
        assertEquals(event, result);
    }
    
    @Test
    public void producerAndConsumerAndHandlerTestWithPayloadWithError() throws InterruptedException, JsonProcessingException {
        String expectedMessage = "{\"event\":\"linked\",\"status\":\"fail\",\"payload\":{\"error\":\"Error\","
            + "\"errorDescription\":\"Error description\",\"id\":\"11111111-1111-4000-1111-111111111111\",\"value\":"
            + "\"22222222-2222-4000-2222-222222222222\"},\"emitted_at\":\"2021-01-29T12:00:00.000Z\",\"trace_id\":"
            + "\"00000000-0000-4000-0000-000000000000\" }";
        UUID traceId = UUID.fromString("00000000-0000-4000-0000-000000000000");
        UUID value = UUID.fromString("22222222-2222-4000-2222-222222222222");
        UUID id = UUID.fromString("11111111-1111-4000-1111-111111111111");
        
        String error = "Error";
        String errorDescription = "Error description";
        
        Payload payload = new Payload();
        payload.setValue(value);
        payload.setId(id);
        payload.setError(error);
        payload.setErrorDescription(errorDescription);
        
        IdentityLinked event = new IdentityLinked();
        event.setStatus(Status.FAIL);
        event.setEmittedAt(LocalDateTime.of(2021, 1, 29, 12, 0, 0, 0));
        event.setTraceId(traceId);
        event.setPayload(payload);
        
        kafkaProducer.sendMessage(event);
        
        ConsumerRecord<String, Message> received = consumerRecords.poll(10, TimeUnit.SECONDS);
        Object receivedValue = Objects.requireNonNull(received).value();
        IdentityLinked result = mapper.readValue(receivedValue.toString(), IdentityLinked.class);
        
        verify(kafkaProducer, times(1)).sendMessage(any());
        verify(kafkaConsumer, never()).listen(any(PersonCreated.class), any(MessageHeaders.class), any(Acknowledgment.class));
        verify(handler, never()).consumeMessage(any());
        
        assertEquals(expectedMessage, receivedValue);
        assertEquals(event, result);
    }
}