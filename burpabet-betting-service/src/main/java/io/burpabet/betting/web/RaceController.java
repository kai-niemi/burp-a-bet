package io.burpabet.betting.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.burpabet.betting.model.Race;
import io.burpabet.betting.service.RaceService;
import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;

@RestController
@RequestMapping(path = "/api/race")
public class RaceController {
    @Autowired
    private RaceService raceService;

    @Autowired
    private RaceResourceAssembler raceResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Race> racePagedResourcesAssembler;

    /**
     * Invoked by web ui to present list of recent bets.
     */
    @GetMapping
    public HttpEntity<PagedModel<RaceModel>> findAllRaces(
            @PageableDefault(size = 30) Pageable page) {
        return ResponseEntity.ok(racePagedResourcesAssembler
                .toModel(raceService.findRaces(page), raceResourceAssembler));
    }

    @GetMapping(value = "/settled")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<PagedModel<RaceModel>> findRacesWithSettledBets(
            @PageableDefault(size = 15) Pageable page) {
        return ResponseEntity.ok(racePagedResourcesAssembler
                .toModel(raceService.findRacesWithSettledBets(page), raceResourceAssembler));
    }

    @GetMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<RaceModel> getRaceById(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(raceResourceAssembler
                .toModel(raceService.getRaceById(id)));
    }

}

