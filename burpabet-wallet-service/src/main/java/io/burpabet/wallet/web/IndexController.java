package io.burpabet.wallet.web;

import org.springframework.hateoas.Link;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/")
public class IndexController {
    @GetMapping
    public IndexModel index() {
        IndexModel index = new IndexModel();

        index.add(linkTo(methodOn(AccountController.class)
                .findAllOperatorAccounts(null, false))
                .withRel("all-operators")
                .withTitle("Operator account collection resource"));

        index.add(linkTo(methodOn(AccountController.class)
                .findAllCustomerAccounts(null))
                .withRel("all-customers")
                .withTitle("Customer account collection resource"));

        index.add(Link.of(ServletUriComponentsBuilder.fromCurrentContextPath()
                        .pathSegment("actuator")
                        .buildAndExpand()
                        .toUriString())
                .withRel("actuators")
                .withTitle("Spring boot actuators"));

        return index;
    }
}
