package io.cockroachdb.betting.common.domain;

import java.util.UUID;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

public class BetSettlementEvent extends OutboxEvent<BetSettlement> {
    public BetSettlementEvent() {
    }

    public BetSettlementEvent(UUID eventId, EventType eventType, BetSettlement payload) {
        super(eventId, eventType, payload);
    }

    @Override
    @JsonDeserialize(as = BetSettlement.class)
    public BetSettlement getPayload() {
        return super.getPayload();
    }
}
