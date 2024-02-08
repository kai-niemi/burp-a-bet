package io.burpabet.betting.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.burpabet.betting.model.Bet;
import io.burpabet.betting.repository.BetRepository;
import io.burpabet.common.annotations.ServiceFacade;
import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;

@ServiceFacade
public class BetService {
    @Autowired
    private BetRepository betRepository;

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Bet> findAll(Pageable page) {
        return betRepository.findAllBets(page);
    }

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Bet> findUnsettledBets(Pageable page) {
        return betRepository.findUnsettledBets(page);
    }

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Bet> findSettledBets(Pageable page) {
        return betRepository.findSettledBets(page);
    }

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Bet findById(UUID id) {
        return betRepository.findById(id)
                .orElseThrow(() -> new NoSuchBetException(id.toString()));
    }
}
