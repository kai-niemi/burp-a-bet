package io.cockroachdb.wallet.saga;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import io.cockroachdb.betting.common.annotations.SagaStepAction;
import io.cockroachdb.betting.common.domain.BetPlacement;
import io.cockroachdb.betting.common.domain.BetPlacementEvent;
import io.cockroachdb.betting.common.domain.BetSettlement;
import io.cockroachdb.betting.common.domain.BetSettlementEvent;
import io.cockroachdb.betting.common.domain.Registration;
import io.cockroachdb.betting.common.domain.RegistrationEvent;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.betting.common.domain.TopicNames;

/**
 * Event listener for the bet placement journey (Saga).
 * <p>
 * The process steps for wallet is to transfer the bet wager
 * amount from the customers account to the operators account.
 * <p>
 * In the event of a Saga rollback, the transfer is reversed.
 */
@Component
@SagaStepAction(description = "Receives registration, bet placement and bet settlement events")
public class WalletKafkaListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WalletBettingFacade bettingFacade;

    @Autowired
    private WalletRegistrationFacade registrationFacade;

    @KafkaListener(id = "registration", topics = TopicNames.REGISTRATION, groupId = "wallet",
            properties = {"spring.json.value.default.type=io.cockroachdb.common.domain.RegistrationEvent"})
    public void onRegistrationEvent(RegistrationEvent event) {
        Registration registration = event.getPayload();

        if (registration.getStatus().equals(Status.PENDING)) {
            registrationFacade.createAccounts(registration);
        } else if (registration.getStatus().equals(Status.ROLLBACK)) {
            registrationFacade.reverseAccounts(registration);
        } else {
            logger.debug("RegistrationEvent received with status: %s".formatted(registration.getStatus()));
        }
    }

    @KafkaListener(id = "placement", topics = TopicNames.PLACEMENT, groupId = "wallet",
            properties = {"spring.json.value.default.type=io.cockroachdb.common.domain.BetPlacementEvent"})
    public void onBetPlacementEvent(BetPlacementEvent event) {
        BetPlacement placement = event.getPayload();

        if (placement.getStatus().equals(Status.PENDING)) {
            bettingFacade.reserveWager(placement);
        } else if (placement.getStatus().equals(Status.ROLLBACK)) {
            bettingFacade.reverseWager(placement);
        } else {
            logger.debug("BetPlacementEvent received with status: %s".formatted(placement.getStatus()));
        }
    }

    @KafkaListener(id = "settlement", topics = TopicNames.SETTLEMENT, groupId = "wallet",
            properties = {"spring.json.value.default.type=io.cockroachdb.common.domain.BetSettlementEvent"})
    public void onBetSettlementEvent(BetSettlementEvent event) {
        BetSettlement settlement = event.getPayload();

        if (settlement.getStatus().equals(Status.PENDING)) {
            bettingFacade.transferPayout(settlement);
        } else {
            logger.debug("BetSettlementEvent received with status: %s".formatted(settlement.getStatus()));
        }
    }
}
