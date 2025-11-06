package io.cockroachdb.betting.common.domain;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.UUIDDeserializer;
import com.fasterxml.jackson.databind.ser.std.UUIDSerializer;

/**
 * A generic outbox event wrapper for CockroachDB CDC queries with 'bare' envelopes.
 *
 * @param <T> the payload generic type (json serde)
 */
public abstract class OutboxEvent<T> {
    // Correlates with the Kafka event key.
    // Projection into payload eventId attribute.
    @JsonProperty("event_id")
    @JsonSerialize(using = UUIDSerializer.class)
    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID eventId;

    // Projection into payload entityId attribute.
    @JsonProperty("aggregate_id")
    @JsonSerialize(using = UUIDSerializer.class)
    @JsonDeserialize(using = UUIDDeserializer.class)
    private UUID aggregateId;

    // When using diff change feeds, provides the CRUD operation type
    @JsonProperty("event_type")
    private EventType eventType;

    private T payload;

    protected OutboxEvent() {
    }

    public OutboxEvent(UUID eventId, EventType eventType, T payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.payload = payload;
    }

    public UUID getEventId() {
        return eventId;
    }

    public EventType getEventType() {
        return eventType;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public String toString() {
        return "OutboxEvent{" +
                "eventId=" + eventId +
                ", aggregateId=" + aggregateId +
                ", eventType=" + eventType +
                ", payload=" + payload +
                '}';
    }
}
