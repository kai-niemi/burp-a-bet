package io.burpabet.common.domain;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class RegistrationEvent extends OutboxEvent<Registration> {
    public RegistrationEvent() {
    }

    public RegistrationEvent(UUID eventId, EventType eventType, Registration payload) {
        super(eventId, eventType, payload);
    }

    @Override
    @JsonDeserialize(as = Registration.class)
    public Registration getPayload() {
        return super.getPayload();
    }
}
