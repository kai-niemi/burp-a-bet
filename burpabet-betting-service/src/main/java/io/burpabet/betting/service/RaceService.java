package io.burpabet.betting.service;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import io.burpabet.betting.model.Race;
import io.burpabet.betting.repository.RaceRepository;
import io.burpabet.common.annotations.ServiceFacade;
import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;

@ServiceFacade
public class RaceService {
    @Autowired
    private RaceRepository raceRepository;

    @TransactionBoundary
    public Race getRandomRace() {
        return raceRepository.getRandomRace().orElseThrow(() -> new IllegalStateException("No races found"));
    }

    @TransactionBoundary
    public Race getRaceById(UUID id) {
        return raceRepository.findById(id).orElseThrow(() -> new NoSuchRaceException(id.toString()));
    }

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Race> findRaces(Pageable page) {
        Page<UUID> raceIds = raceRepository.findRaceIds(page);
        List<Race> races = raceRepository.findRaces(raceIds.getContent());
        return new PageImpl<>(races, page, raceIds.getTotalElements());
    }

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Race> findRacesWithSettledBets(Pageable page) {
        Page<UUID> raceIds = raceRepository.findRaceIdsWithSettledBets(page);
        List<Race> races = raceRepository.findRacesWithSettledBets(raceIds.getContent());
        return new PageImpl<>(races, page, raceIds.getTotalElements());
    }

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Race> findRacesWithUnsettledBets(Pageable page) {
        Page<UUID> raceIds = raceRepository.findRaceIdsWithUnsettledBets(page);
        List<Race> races = raceRepository.findRacesWithUnsettledBets(raceIds.getContent());
        return new PageImpl<>(races, page, raceIds.getTotalElements());
    }
}
