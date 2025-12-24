package io.cockroachdb.betting.service;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import io.cockroachdb.betting.model.Bet;
import io.cockroachdb.betting.repository.BetRepository;
import io.cockroachdb.betting.common.annotations.ServiceFacade;
import io.cockroachdb.betting.common.annotations.FollowerRead;
import io.cockroachdb.betting.common.annotations.FollowerReadMode;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;

@ServiceFacade
public class BetService {
    @Autowired
    private BetRepository betRepository;

    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public Page<Bet> findAll(Pageable page) {
        return betRepository.findAllBets(page);
    }

    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public Page<Bet> findUnsettledBets(Pageable page) {
        return betRepository.findUnsettledBets(page);
    }

    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public Page<Bet> findSettledBets(Pageable page) {
        return betRepository.findSettledBets(page);
    }

    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public Bet findById(UUID id) {
        return betRepository.findById(id)
                .orElseThrow(() -> new NoSuchBetException(id.toString()));
    }
}
