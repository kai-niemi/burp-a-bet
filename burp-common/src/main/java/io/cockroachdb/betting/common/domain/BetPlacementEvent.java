package io.cockroachdb.betting.common.domain;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.UUID;

public class BetPlacementEvent extends OutboxEvent<BetPlacement> {
    public BetPlacementEvent() {
    }

    public BetPlacementEvent(UUID eventId, EventType eventType, BetPlacement payload) {
        super(eventId, eventType, payload);
    }

    @Override
    @JsonDeserialize(as = BetPlacement.class)
    public BetPlacement getPayload() {
        return super.getPayload();
    }
}
