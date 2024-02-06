package io.burpabet.betting.service;

import io.burpabet.betting.model.Bet;
import io.burpabet.betting.model.Race;
import io.burpabet.betting.repository.BetRepository;
import io.burpabet.betting.repository.RaceRepository;
import io.burpabet.common.annotations.OutboxOperation;
import io.burpabet.common.annotations.Retryable;
import io.burpabet.common.annotations.ServiceFacade;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.BetPlacement;
import io.burpabet.common.domain.BetPlacementEvent;
import io.burpabet.common.domain.EventType;
import io.burpabet.common.domain.Status;
import io.burpabet.common.shell.DebugSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import java.util.Optional;
import java.util.UUID;

@ServiceFacade
public class BetPlacementService {
    private static BetPlacement toBetPlacement(Bet bet) {
        BetPlacement betPlacement = new BetPlacement();
        betPlacement.setEntityId(bet.getId());
        betPlacement.setStatus(bet.getPlacementStatus());
        betPlacement.setCustomerId(bet.getCustomerId());
        betPlacement.setJurisdiction(bet.getJurisdiction());
        betPlacement.setStake(bet.getStake());
        betPlacement.setRaceId(bet.getRace().getId());
        betPlacement.setHorse(bet.getRace().getHorse());
        betPlacement.setTrack(bet.getRace().getTrack());
        return betPlacement;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BetRepository betRepository;

    @Autowired
    private RaceRepository raceRepository;

    @TransactionBoundary
    @OutboxOperation(aggregateType = "placement")
    @Retryable
    public BetPlacement placeBet(BetPlacement betPlacement) {
        Race race = raceRepository.findByIdForShare(betPlacement.getRaceId())
                .orElseThrow(() -> new NoSuchRaceException(betPlacement.getRaceId().toString()));

        Bet bet = new Bet();
        bet.setRace(race);
        bet.setBetType(betPlacement.getBetType());
        bet.setCustomerId(betPlacement.getCustomerId());
        bet.setCustomerName(betPlacement.getCustomerName());
        bet.setStake(betPlacement.getStake());
        bet.setPlacementStatus(Status.PENDING);

        bet = betRepository.save(bet);

        BetPlacement placement = toBetPlacement(bet);
        placement.setEventId(UUID.randomUUID());

        return placement;
    }

    @TransactionBoundary
    @Retryable
    public BetPlacementEvent confirmPlacement(BetPlacementEvent fromWallet, BetPlacementEvent fromCustomer) {
        BetPlacement walletPayload = fromWallet.getPayload();
        BetPlacement customerPayload = fromCustomer.getPayload();

        Optional<Bet> optional = betRepository.findById(walletPayload.getEntityId());
        if (optional.isEmpty()) {
            logger.warn("Bet not found: {}", walletPayload.getEntityId());
            return null;
        }

        Bet bet = optional.get();
        bet.setJurisdiction(customerPayload.getJurisdiction());
        bet.setCustomerName(customerPayload.getCustomerName());

        String origin = "betting-service";

        if (walletPayload.getStatus().equals(Status.APPROVED) &&
                customerPayload.getStatus().equals(Status.APPROVED)) {
            bet.setPlacementStatus(Status.APPROVED);
        } else if (walletPayload.getStatus().equals(Status.REJECTED) &&
                customerPayload.getStatus().equals(Status.REJECTED)) {
            bet.setPlacementStatus(Status.REJECTED);
        } else if (walletPayload.getStatus().equals(Status.REJECTED) ||
                customerPayload.getStatus().equals(Status.REJECTED)) {
            bet.setPlacementStatus(Status.ROLLBACK);
            origin = walletPayload.getStatus().equals(Status.REJECTED)
                    ? walletPayload.getOrigin() : customerPayload.getOrigin();
        }

        DebugSupport.logJourneyCompletion(logger,
                "Bet Placement",
                Pair.of("Wallet", walletPayload),
                Pair.of("Customer", customerPayload),
                bet.getPlacementStatus());

        BetPlacement placement = toBetPlacement(bet);
        placement.setEventId(fromWallet.getEventId());
        placement.setOrigin(origin);

        return new BetPlacementEvent(fromWallet.getEventId(), EventType.insert, placement);
    }

}
