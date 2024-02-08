package io.burpabet.betting.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import io.burpabet.betting.model.Bet;
import io.burpabet.betting.model.Race;
import io.burpabet.betting.repository.BetRepository;
import io.burpabet.betting.repository.RaceRepository;
import io.burpabet.common.annotations.Retryable;
import io.burpabet.common.annotations.ServiceFacade;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.BetSettlement;
import io.burpabet.common.domain.BetSettlementEvent;
import io.burpabet.common.domain.EventType;
import io.burpabet.common.domain.Outcome;
import io.burpabet.common.domain.Status;
import io.burpabet.common.outbox.OutboxRepository;
import io.burpabet.common.shell.DebugSupport;

@ServiceFacade
public class BetSettlementService {
    private static final String TOPIC_BET_SUMMARY = "/topic/bet-summary";

    private static BetSettlement toBetSettlement(Bet bet) {
        BetSettlement betSettlement = new BetSettlement();
        betSettlement.setEntityId(bet.getId());
        betSettlement.setStatus(bet.getSettlementStatus());
        betSettlement.setCustomerId(bet.getCustomerId());
        betSettlement.setPayout(bet.getPayout());
        return betSettlement;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @TransactionBoundary
    public void deleteAllInBatch() {
        betRepository.deleteAllInBatch();
        outboxRepository.deleteAllInBatch();
    }

    @TransactionBoundary
    @Retryable
    public void settleBets(Race detached, Outcome outcome) {
        Race race = raceRepository.findByIdForShare(detached.getId())
                .orElseThrow(() -> new NoSuchRaceException(detached.getId().toString()));
        race.setOutcome(outcome);
        race.getBets()
                .stream()
                .filter(bet -> !bet.isSettled())
                .forEach(bet -> {
                    switch (outcome) {
                        case win -> bet.setPayout(bet.getStake()
                                .multiply(bet.getRace().getOdds())
                                .plus(bet.getStake()));
                        case lose -> bet.setPayout(bet.getStake().negate());
                    }
                    bet.setSettlementStatus(Status.PENDING);

                    BetSettlement settlement = toBetSettlement(bet);
                    settlement.setEventId(UUID.randomUUID());
                    settlement.setOrigin("betting-service");

                    outboxRepository.writeEvent(settlement, "settlement");
                });
    }

    @TransactionBoundary
    @Retryable
    public BetSettlementEvent confirmSettlement(BetSettlementEvent fromWallet, BetSettlementEvent fromCustomer) {
        BetSettlement walletPayload = fromWallet.getPayload();
        BetSettlement customerPayload = fromCustomer.getPayload();

        Optional<Bet> optional = betRepository.findById(walletPayload.getEntityId());
        if (optional.isEmpty()) {
            logger.warn("Bet not found with id: {}", walletPayload.getEntityId());
            return null;
        }

        Bet bet = optional.get();

        String origin = null;

        if (walletPayload.getStatus().equals(Status.APPROVED) &&
                customerPayload.getStatus().equals(Status.APPROVED)) {
            bet.setSettled(true);
            bet.setSettlementStatus(Status.APPROVED);
        } else if (walletPayload.getStatus().equals(Status.REJECTED) &&
                customerPayload.getStatus().equals(Status.REJECTED)) {
            bet.setSettled(false);
            bet.setSettlementStatus(Status.REJECTED);
        } else if (walletPayload.getStatus().equals(Status.REJECTED) ||
                customerPayload.getStatus().equals(Status.REJECTED)) {
            bet.setSettled(false);
            bet.setSettlementStatus(Status.ROLLBACK);

            origin = walletPayload.getStatus().equals(Status.REJECTED)
                    ? walletPayload.getOrigin() : customerPayload.getOrigin();
        }

        DebugSupport.logJourneyCompletion(logger,
                "Bet Settlement",
                Pair.of("Wallet", walletPayload),
                Pair.of("Customer", customerPayload),
                bet.getSettlementStatus());

        BetSettlement settlement = toBetSettlement(bet);
        settlement.setEventId(fromWallet.getEventId());
        settlement.setOrigin(origin);

        simpMessagingTemplate.convertAndSend(TOPIC_BET_SUMMARY, settlement);

        return new BetSettlementEvent(fromWallet.getEventId(), EventType.insert, settlement);
    }
}
