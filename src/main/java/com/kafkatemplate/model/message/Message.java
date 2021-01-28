package com.kafkatemplate.model.message;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import java.time.LocalDateTime;
import java.util.UUID;
import com.kafkatemplate.enums.Status;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = Id.NAME, include = As.EXISTING_PROPERTY, property = "event")
@JsonSubTypes({
    @Type(value = PersonCreated.class, name = Message.PERSON_CREATED),
    @Type(value = IdentityLinked.class, name = Message.IDENTITY_LINKED),
    @Type(value = StatusCheck.class, name = Message.STATUS_CHECK)
})
public abstract class Message {

    public static final String PERSON_CREATED = "created";
    public static final String IDENTITY_LINKED = "linked";
    public static final String STATUS_CHECK = "status";
    private final String event;

    @JsonProperty("emitted_at")
    @JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    @JsonDeserialize(using=LocalDateTimeDeserializer.class)
    @JsonSerialize(using=LocalDateTimeSerializer.class)
    private LocalDateTime emittedAt;
    
    private Status status;
    
    @JsonProperty("correlation_id")
    private UUID traceId;

    public Message(String eventType) {
        this.event = eventType;
    }

    public String getEvent() {
        return event;
    }

    public LocalDateTime getEmittedAt() {
        return emittedAt;
    }

    public void setEmittedAt(LocalDateTime emittedAt) {
        this.emittedAt = emittedAt;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public UUID getTraceId() {
        return traceId;
    }

    public void setTraceId(UUID traceId) {
        this.traceId = traceId;
    }
}