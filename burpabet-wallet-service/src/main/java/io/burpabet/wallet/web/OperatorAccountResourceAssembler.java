package io.burpabet.wallet.web;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.burpabet.wallet.model.OperatorAccount;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class OperatorAccountResourceAssembler implements SimpleRepresentationModelAssembler<OperatorAccount> {
    @Override
    public void addLinks(EntityModel<OperatorAccount> resource) {
        OperatorAccount account = resource.getContent();
        resource.add(linkTo(methodOn(AccountController.class)
                .getOperatorAccount(account.getId()))
                .withSelfRel()
                .andAffordance(afford(methodOn(AccountController.class)
                        .updateAccount(account.getId(), null)))
                .andAffordance(afford(methodOn(AccountController.class)
                        .deleteAccount(account.getId())))
        );
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<OperatorAccount>> resources) {

    }
}
