package com.kafkatemplate.model.message;

import com.kafkatemplate.model.payload.Payload;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class IdentityLinked extends Message {
    
    private Payload payload;
    
    public IdentityLinked() {
        super(IDENTITY_LINKED);
    }

    public Payload getPayload() {
        return payload;
    }

    public void setPayload(Payload payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        IdentityLinked that = (IdentityLinked) o;
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload);
    }

    @Override
    public String toString() {
        return "IdentityLinked{"
                + "payload=" + payload
                + '}';
    }
}