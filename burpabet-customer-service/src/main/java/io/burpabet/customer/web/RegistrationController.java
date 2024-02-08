package io.burpabet.customer.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.mediatype.Affordances;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.domain.Registration;
import io.burpabet.common.domain.Status;
import io.burpabet.common.util.RandomData;
import io.burpabet.customer.model.Customer;
import io.burpabet.customer.service.CustomerService;
import jakarta.validation.Valid;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@RestController
@RequestMapping(path = "/api/registration")
public class RegistrationController {
    @Autowired
    private CustomerService customerService;

    @GetMapping(value = "/")
    public ResponseEntity<RegistrationModel> getRegistrationForm() {
        Pair<String, String> pair = RandomData.randomFullNameAndEmail("burpabet.io");

        RegistrationModel form = new RegistrationModel();
        form.setName(pair.getFirst());
        form.setEmail(pair.getSecond());
        form.setJurisdiction(Jurisdiction.SE);
        form.setOperatorId(UUID.randomUUID());

        form.add(Affordances.of(linkTo(methodOn(getClass()).getRegistrationForm())
                        .withSelfRel()
                        .andAffordance(afford(methodOn(getClass())
                                .register(null))))
                .toLink());

        return ResponseEntity.ok(form);
    }

    @PostMapping(value = "/")
    public HttpEntity<RegistrationModel> register(@Valid @RequestBody RegistrationModel form) {
        Customer customer = Customer.builder()
                .withName(form.getName())
                .withEmail(form.getEmail())
                .withJurisdiction(form.getJurisdiction())
                .withStatus(Status.PENDING)
                .withOperatorId(form.getOperatorId())
                .build();

        Registration registration = customerService.registerCustomer(customer);

        Link selfLink = linkTo(methodOn(CustomerController.class)
                .getCustomer(registration.getEntityId()))
                .withSelfRel();

        RegistrationModel resource = new RegistrationModel();
        resource.add(linkTo(methodOn(CustomerController.class)
                .getCustomer(registration.getEntityId())).withSelfRel()
                .andAffordance(afford(methodOn(CustomerController.class)
                        .updateCustomer(registration.getEntityId(), null)))
                .andAffordance(afford(methodOn(CustomerController.class)
                        .deleteCustomer(registration.getEntityId())))
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .location(selfLink.toUri())
                .body(resource);
    }
}
