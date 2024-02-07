package io.burpabet.betting.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.burpabet.betting.model.Bet;
import io.burpabet.betting.repository.BetRepository;
import io.burpabet.betting.service.BetPlacementService;
import io.burpabet.betting.service.NoSuchBetException;
import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.BetPlacement;
import io.burpabet.common.util.Money;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api/bet")
public class BetController {
    @Autowired
    private BetRepository betRepository;

    @Autowired
    private BetResourceAssembler betResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Bet> betPagedResourcesAssembler;

    @GetMapping
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<CollectionModel<EntityModel<Bet>>> findAll(
            @PageableDefault(size = 15) Pageable page) {
        Page<Bet> bets = betRepository.findAllBets(page);
        return ResponseEntity.ok(betPagedResourcesAssembler.toModel(bets, betResourceAssembler));
    }

    @GetMapping(value = "/unsettled")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<CollectionModel<EntityModel<Bet>>> findAllUnSettled(
            @PageableDefault(size = 15) Pageable page) {
        Page<Bet> bets = betRepository.findUnsettledBets(page);
        return ResponseEntity.ok(betPagedResourcesAssembler.toModel(bets, betResourceAssembler));
    }

    /**
     * Invoked by web ui to present list of recent bets.
     */
    @GetMapping(value = "/settled")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<CollectionModel<EntityModel<Bet>>> findAllSettled(
            @PageableDefault(size = 30) Pageable page) {
        Page<Bet> bets = betRepository.findSettledBets(page);
        return ResponseEntity.ok(betPagedResourcesAssembler.toModel(bets, betResourceAssembler));
    }

    @GetMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<EntityModel<Bet>> getBet(@PathVariable("id") UUID id) {
        Bet bet = betRepository.findById(id)
                .orElseThrow(() -> new NoSuchBetException(id.toString()));
        return ResponseEntity.ok(betResourceAssembler.toModel(bet));
    }

    @Autowired
    private BetPlacementService betPlacementService;

    @GetMapping(value = "/form")
    public ResponseEntity<BetModel> getBetPlacementForm() {
        BetModel form = new BetModel();
        form.setStake(Money.of("5.00", "USD"));
        form.setIdempotencyKey(UUID.randomUUID());
        form.setCustomerId(UUID.randomUUID());
        form.setRaceId(betPlacementService.getRandomRace().getId());

        form.add(Affordances.of(linkTo(methodOn(getClass()).getBetPlacementForm())
                        .withSelfRel()
                        .andAffordance(afford(methodOn(getClass())
                                .placeBet(null))))
                .toLink());

        return ResponseEntity.ok(form);
    }

    @PostMapping(value = "/form")
    public HttpEntity<BetModel> placeBet(@Valid @RequestBody BetModel form) {
        BetPlacement betPlacement = new BetPlacement();
        betPlacement.setEventId(form.getIdempotencyKey());
        betPlacement.setCustomerId(form.getCustomerId());
        betPlacement.setRaceId(form.getRaceId());
        betPlacement.setStake(form.getStake());

        betPlacement = betPlacementService.placeBet(betPlacement);

        Link selfLink = linkTo(methodOn(getClass())
                .getBet(betPlacement.getEntityId()))
                .withSelfRel();

        return ResponseEntity.created(selfLink.toUri()).build();
    }
}
