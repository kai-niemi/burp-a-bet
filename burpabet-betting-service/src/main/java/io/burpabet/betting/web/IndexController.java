package io.burpabet.betting.web;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api")
public class IndexController {
    @GetMapping
    public IndexModel index() {
        IndexModel index = new IndexModel();
        index.add(linkTo(methodOn(BetController.class)
                .findAll(null))
                .withRel("all")
                .withTitle("Collection of bets"));
        index.add(linkTo(methodOn(BetController.class)
                .findAllSettled(null))
                .withRel("settled")
                .withTitle("Collection of settled bets"));
        index.add(linkTo(methodOn(BetController.class)
                .findAllUnSettled(null))
                .withRel("unsettled")
                .withTitle("Collection of unsettled bets"));

        index.add(linkTo(methodOn(RaceController.class)
                .findAllRaces(null))
                .withRel("all")
                .withTitle("Collection of races"));
        index.add(linkTo(methodOn(RaceController.class)
                .findRacesWithSettledBets(null))
                .withRel("settled")
                .withTitle("Collection of races with settle bets"));

        index.add(linkTo(methodOn(PlacementController.class)
                .getPlacementForm())
                .withRel("placement")
                .withTitle("Form template for placing a bet"));
        index.add(linkTo(methodOn(SettlementController.class)
                .getSettlementForm())
                .withRel("settlement")
                .withTitle("Form template for settling bets"));

//        index.add(linkTo(methodOn(CustomerController.class)
//                .findAll(null,null))
//                .withRel("customer:customer-list")
//                .withTitle("Collection of customers (relay to betting-customer service)"));

        return index;
    }

}
