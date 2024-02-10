package io.burpabet.betting.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.burpabet.betting.model.Race;
import io.burpabet.betting.service.BetSettlementService;
import io.burpabet.betting.service.RaceService;
import io.burpabet.common.domain.Outcome;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api/settlement")
public class SettlementController {
    @Autowired
    private RaceService raceService;

    @Autowired
    private BetSettlementService betSettlementService;

    @GetMapping(value = "/")
    public ResponseEntity<SettlementModel> getSettlementForm() {
        SettlementModel form = new SettlementModel();
        form.setOutcome(Outcome.win);
        form.setRaceId(raceService.getRandomRace().getId());

        form.add(Affordances.of(linkTo(methodOn(getClass()).getSettlementForm())
                        .withSelfRel()
                        .andAffordance(afford(methodOn(getClass())
                                .settleBets(null))))
                .toLink());
        form.add(Affordances.of(linkTo(methodOn(RaceController.class)
                        .getRaceById(form.getRaceId()))
                        .withRel("race"))
                .toLink());

        return ResponseEntity.ok(form);
    }

    @PostMapping(value = "/")
    public HttpEntity<SettlementModel> settleBets(@Valid @RequestBody SettlementModel form) {
        UUID raceId = form.getRaceId();
        if (raceId != null) {
            betSettlementService.settleBets(raceId, form.getOutcome());
        } else {
            Page<Race> page = raceService.findRacesWithUnsettledBets(PageRequest.ofSize(form.getPageSize()));
            for (; ; ) {
                page.forEach(x -> betSettlementService.settleBets(x.getId(), form.getOutcome()));
                if (page.hasNext()) {
                    page = raceService.findRacesWithUnsettledBets(page.nextPageable());
                } else {
                    break;
                }
            }
        }
        return ResponseEntity.accepted().build();
    }
}
