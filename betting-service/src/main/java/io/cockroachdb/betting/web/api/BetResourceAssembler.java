package io.cockroachdb.betting.web.api;

import io.cockroachdb.betting.model.Bet;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class BetResourceAssembler implements SimpleRepresentationModelAssembler<Bet> {
    @Override
    public void addLinks(EntityModel<Bet> resource) {
        Bet bet = resource.getContent();
        resource.add(linkTo(methodOn(BetController.class)
                .getBet(bet.getId())).withSelfRel()
        );
        resource.add(linkTo(methodOn(RaceController.class)
                .getRaceById(bet.getRace().getId()))
                .withRel("race")
        );
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Bet>> resources) {

    }
}
