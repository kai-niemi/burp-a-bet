package io.burpabet.customer.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.burpabet.common.annotations.SagaStepAction;
import io.burpabet.common.domain.BetPlacement;
import io.burpabet.common.domain.BetPlacementEvent;
import io.burpabet.common.domain.BetSettlement;
import io.burpabet.common.domain.BetSettlementEvent;
import io.burpabet.common.domain.Status;
import io.burpabet.common.domain.TopicNames;

/**
 * Event listener for the bet placement journey (Saga).
 * <p>
 * The process steps for customer is to debit the spending budget
 * for the bet wager amount. This step is approved only
 * if the spending limit is not exceeded.
 * <p>
 * In the event of a Saga rollback, the spending limit is restored
 * to the previous state.
 */
@Component
@SagaStepAction(description = "Receives bet placement and bet settlement events")
public class CustomerKafkaListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerBettingFacade customerBettingFacade;

    @KafkaListener(id = "placement", topics = TopicNames.PLACEMENT, groupId = "customer",
            properties = {"spring.json.value.default.type=io.burpabet.common.domain.BetPlacementEvent"})
    public void onBetPlacementEvent(BetPlacementEvent event) {
        BetPlacement placement = event.getPayload();

        if (placement.getStatus().equals(Status.PENDING)) {
            customerBettingFacade.acquireSpendingCredits(placement);
        } else if (placement.getStatus().equals(Status.ROLLBACK)) {
            customerBettingFacade.releaseSpendingCredits(placement);
        } else {
            logger.info("BetPlacement event received with status: %s".formatted(placement.getStatus()));
        }
    }

    @KafkaListener(id = "settlement", topics = TopicNames.SETTLEMENT, groupId = "customer",
            properties = {"spring.json.value.default.type=io.burpabet.common.domain.BetSettlementEvent"})
    public void onBetSettlementEvent(BetSettlementEvent event) {
        BetSettlement settlement = event.getPayload();

        if (settlement.getStatus().equals(Status.PENDING)) {
            customerBettingFacade.approveSettlement(settlement);
        } else {
            logger.info("BetSettlement event received with status: %s".formatted(settlement.getStatus()));
        }
    }
}
