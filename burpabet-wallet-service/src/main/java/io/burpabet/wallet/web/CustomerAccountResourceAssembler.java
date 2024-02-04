package io.burpabet.wallet.web;

import java.util.UUID;

import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import io.burpabet.wallet.model.CustomerAccount;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CustomerAccountResourceAssembler implements SimpleRepresentationModelAssembler<CustomerAccount> {
    @Override
    public void addLinks(EntityModel<CustomerAccount> resource) {
        CustomerAccount account = resource.getContent();
        resource.add(linkTo(methodOn(AccountController.class)
                .getCustomerAccount(account.getId())).withSelfRel()
                .andAffordance(afford(methodOn(AccountController.class)
                        .updateAccount(account.getId(), null)))
                .andAffordance(afford(methodOn(AccountController.class)
                        .deleteAccount(account.getId())))
        );

        UUID operatorId = account.getOperatorId();
        resource.add(linkTo(methodOn(AccountController.class)
                .getCustomerAccount(operatorId))
                .withRel("operator-account")
                .withTitle("Assigned operator"));
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<CustomerAccount>> resources) {
    }
}
