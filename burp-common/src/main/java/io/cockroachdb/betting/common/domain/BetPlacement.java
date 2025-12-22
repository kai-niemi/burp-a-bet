package io.cockroachdb.betting.common.domain;

import java.util.Map;
import java.util.UUID;

import io.cockroachdb.betting.common.util.Money;

/**
 * DTO representing the state of a customer bet placement journey (Saga).
 */
public class BetPlacement extends AbstractJourney {
    private UUID customerId;

    private String customerName;

    private UUID raceId;

    private Money stake;

    private BetType betType = BetType.win;

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public BetType getBetType() {
        return betType;
    }

    public void setBetType(BetType betType) {
        this.betType = betType;
    }

    public UUID getRaceId() {
        return raceId;
    }

    public void setRaceId(UUID raceId) {
        this.raceId = raceId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Money getStake() {
        return stake;
    }

    public void setStake(Money stake) {
        this.stake = stake;
    }

    @Override
    public void debugTuples(Map<String, Object> console) {
        console.put("customerId", customerId);
        console.put("customerName", customerName);
        console.put("raceId", raceId);
        console.put("stake", stake);
        console.put("jurisdiction", getJurisdiction());
        console.put("entityId", getEntityId());
        console.put("status", getStatus());
        console.put("statusDetail", getStatusDetail());
        console.put("origin", getOrigin());
    }

    @Override
    public String toString() {
        return "BetPlacement{" +
                "customerId=" + customerId +
                ", customerName='" + customerName + '\'' +
                ", raceId=" + raceId +
                ", stake=" + stake +
                ", betType=" + betType +
                "} " + super.toString();
    }
}
