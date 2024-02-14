package io.burpabet.customer.web;

import java.util.EnumSet;

import io.burpabet.common.domain.Jurisdiction;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.burpabet.common.domain.Status;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api")
public class IndexController {
    @GetMapping
    public IndexModel index() {
        IndexModel index = new IndexModel();
        index.add(linkTo(methodOn(CustomerController.class)
                .findAll( null))
                .withRel("all")
                .withTitle("Collection of customers"));

        index.add(linkTo(methodOn(CustomerController.class)
                .findAnyCustomer())
                .withRel("one")
                .withTitle("Any customer"));

        EnumSet.allOf(Jurisdiction.class).forEach(jurisdiction -> {
            index.add(linkTo(methodOn(CustomerController.class)
                    .findAllWithJurisdiction(jurisdiction, null))
                    .withRel("jurisdiction")
                    .withTitle("Collection of customers in a given jurisdiction"));
        });

        EnumSet.allOf(Status.class).forEach(status -> {
            index.add(linkTo(methodOn(CustomerController.class)
                    .findAllWithStatus(status, null))
                    .withRel("status")
                    .withTitle("Collection of customers with status " + status));
        });

        index.add(linkTo(methodOn(RegistrationController.class)
                .getRegistrationForm())
                .withRel("registration")
                .withTitle("Customer registration form"));

        return index;
    }
}
