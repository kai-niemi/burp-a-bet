package io.burpabet.betting.service;

import io.burpabet.betting.model.Race;
import io.burpabet.betting.repository.BetRepository;
import io.burpabet.betting.repository.RaceRepository;
import io.burpabet.common.annotations.ServiceFacade;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.outbox.OutboxRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

@ServiceFacade
public class BettingService {
    @Autowired
    private BetRepository betRepository;

    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @TransactionBoundary
    public void deleteAllInBatch() {
        betRepository.deleteAllInBatch();
        outboxRepository.deleteAllInBatch();
    }

    @TransactionBoundary
    public Race getRandomRace() {
        return raceRepository.getRandomRace().orElseThrow(() -> new IllegalStateException("No races found"));
    }

    @TransactionBoundary
    public Race getRaceById(UUID id) {
        return raceRepository.findById(id).orElseThrow(() -> new NoSuchRaceException(id.toString()));
    }

    @TransactionBoundary
    public Page<Race> findRacesWithUnsettledBets(Pageable page) {
        Page<UUID> raceIds = raceRepository.findRaceIdsWithUnsettledBets(page);
        List<Race> races = raceRepository.findRacesWithUnsettledBets(raceIds.getContent());
        return new PageImpl<>(races, page, raceIds.getTotalElements());
    }
}
