package io.burpabet.betting.web.api;

import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.stereotype.Component;

import io.burpabet.betting.model.Race;
import io.burpabet.common.domain.Outcome;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class RaceResourceAssembler
        extends RepresentationModelAssemblerSupport<Race, RaceModel> {
    public RaceResourceAssembler() {
        super(RaceController.class, RaceModel.class);
    }

    @Override
    public RaceModel toModel(Race entity) {
        RaceModel model = new RaceModel();
        model.setTrack(entity.getTrack());
        model.setHorse(entity.getHorse());
        model.setOdds(entity.getOdds());
        model.setTotalPayout(entity.getTotalPayout());
        model.setTotalWager(entity.getTotalWager());
        model.setTotalBets(entity.getTotalBets());
        model.setOutcome(entity.getOutcome() != null ? entity.getOutcome() : Outcome.pending);
        model.setDate(entity.getDate());

        model.add(linkTo(methodOn(RaceController.class)
                .getRaceById(entity.getId()))
                .withSelfRel()
        );

        return model;
    }
}

