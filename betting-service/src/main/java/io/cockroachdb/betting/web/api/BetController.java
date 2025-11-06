package io.cockroachdb.betting.web.api;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.betting.model.Bet;
import io.cockroachdb.betting.service.BetService;

@RestController
@RequestMapping(path = "/api/bet")
public class BetController {
    @Autowired
    private BetService betService;

    @Autowired
    private BetResourceAssembler betResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Bet> betPagedResourcesAssembler;

    @GetMapping
    public HttpEntity<CollectionModel<EntityModel<Bet>>> findAll(
            @PageableDefault(size = 15) Pageable page) {
        return ResponseEntity.ok(betPagedResourcesAssembler
                .toModel(betService.findAll(page), betResourceAssembler));
    }

    @GetMapping(value = "/unsettled")
    public HttpEntity<CollectionModel<EntityModel<Bet>>> findAllUnSettled(
            @PageableDefault(size = 15) Pageable page) {
        return ResponseEntity.ok(betPagedResourcesAssembler
                .toModel(betService.findUnsettledBets(page), betResourceAssembler));
    }

    /**
     * Invoked by web ui to present list of recent bets.
     */
    @GetMapping(value = "/settled")
    public HttpEntity<CollectionModel<EntityModel<Bet>>> findAllSettled(
            @PageableDefault(size = 30) Pageable page) {
        return ResponseEntity.ok(betPagedResourcesAssembler
                .toModel(betService.findSettledBets(page), betResourceAssembler));
    }

    @GetMapping(value = "/{id}")
    public HttpEntity<EntityModel<Bet>> getBet(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(betResourceAssembler.toModel(betService.findById(id)));
    }
}
