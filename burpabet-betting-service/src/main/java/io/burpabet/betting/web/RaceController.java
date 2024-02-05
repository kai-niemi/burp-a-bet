package io.burpabet.betting.web;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.burpabet.betting.model.Race;
import io.burpabet.betting.repository.RaceRepository;
import io.burpabet.betting.service.NoSuchRaceException;
import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;

@RestController
@RequestMapping(path = "/api/race")
public class RaceController {
    @Autowired
    private RaceRepository raceRepository;

    @Autowired
    private RaceResourceAssembler raceResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Race> racePagedResourcesAssembler;

    /**
     * Invoked by web ui to present list of recent bets.
     */
    @GetMapping
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<PagedModel<RaceModel>> findAllRaces(
            @PageableDefault(size = 30) Pageable page) {
        // Two queries to avoid in-memory sorting by hibernate due to left join fetch with limit
        Page<UUID> raceIds = raceRepository.findRaceIds(page);
        List<Race> races = raceRepository.findRaces(raceIds.getContent());
        return ResponseEntity.ok(racePagedResourcesAssembler.toModel(
                new PageImpl<>(races, page, raceIds.getTotalElements()), raceResourceAssembler));
    }

    @GetMapping(value = "/settled")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<PagedModel<RaceModel>> findRacesWithSettledBets(
            @PageableDefault(size = 15) Pageable page) {
        Page<UUID> raceIds = raceRepository.findRaceIdsWithSettledBets(page);
        List<Race> races = raceRepository.findRacesWithSettledBets(raceIds.getContent());
        return ResponseEntity.ok(racePagedResourcesAssembler.toModel(
                new PageImpl<>(races, page, raceIds.getTotalElements()), raceResourceAssembler));
    }

    @GetMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<RaceModel> getRace(@PathVariable("id") UUID id) {
        Race race = raceRepository.findById(id)
                .orElseThrow(() -> new NoSuchRaceException(id.toString()));
        return ResponseEntity.ok(raceResourceAssembler.toModel(race));
    }
}

