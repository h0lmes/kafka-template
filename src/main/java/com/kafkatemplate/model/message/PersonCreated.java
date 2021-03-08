package com.kafkatemplate.model.message;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.kafkatemplate.model.payload.Payload;

import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PersonCreated extends Message {

    private Payload payload;

    public PersonCreated() {
        super(PERSON_CREATED);
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
        PersonCreated that = (PersonCreated) o;
        return Objects.equals(payload, that.payload);
    }

    @Override
    public int hashCode() {
        return Objects.hash(payload);
    }

    @Override
    public String toString() {
        return "PersonCreated{"
                + "payload=" + payload
                + '}';
    }
}