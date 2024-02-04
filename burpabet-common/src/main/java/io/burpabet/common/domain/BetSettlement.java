package io.burpabet.common.domain;

import java.util.Map;
import java.util.UUID;

import io.burpabet.common.util.Money;

/**
 * DTO representing the state of a customer bet settlement journey (Saga).
 */
public class BetSettlement extends AbstractJourney {
    private UUID customerId;

    private Money payout;

    public UUID getCustomerId() {
        return customerId;
    }

    public void setCustomerId(UUID customerId) {
        this.customerId = customerId;
    }

    public Money getPayout() {
        return payout;
    }

    public void setPayout(Money payout) {
        this.payout = payout;
    }

    @Override
    public void debugTuples(Map<String, Object> console) {
        console.put("customerId", customerId);
        console.put("payout", payout);
        console.put("jurisdiction", getJurisdiction());
        console.put("entityId", getEntityId());
        console.put("status", getStatus());
        console.put("statusDetail", getStatusDetail());
        console.put("origin", getOrigin());
    }

    @Override
    public String toString() {
        return "BetSettlement{" +
                "customerId=" + customerId +
                ", payout=" + payout +
                "} " + super.toString();
    }
}
