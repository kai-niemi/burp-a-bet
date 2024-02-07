package io.burpabet.betting.web;

import java.util.UUID;

import org.springframework.hateoas.RepresentationModel;
import org.springframework.hateoas.server.core.Relation;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import io.burpabet.common.util.Money;
import jakarta.validation.constraints.NotNull;

@Relation(value = "betting:bet",
        collectionRelation = "betting:bet-list")
@JsonPropertyOrder({"links"})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BetModel extends RepresentationModel<BetModel> {
    private UUID idempotencyKey;

    @NotNull
    private UUID customerId;

    @NotNull
    private UUID raceId;

    @NotNull
    private Money stake;

    public UUID getIdempotencyKey() {
        return idempotencyKey;
    }

    public void setIdempotencyKey(UUID idempotencyKey) {
        this.idempotencyKey = idempotencyKey;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public UUID getRaceId() {
        return raceId;
    }

    public void setRaceId(UUID raceId) {
        this.raceId = raceId;
    }

    public Money getStake() {
        return stake;
    }

    public void setStake(Money stake) {
        this.stake = stake;
    }
}
