package io.burpabet.customer.web;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;

import io.burpabet.customer.model.Customer;
import io.burpabet.customer.shell.HypermediaClient;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.afford;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Component
public class CustomerResourceAssembler implements SimpleRepresentationModelAssembler<Customer> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private HypermediaClient hypermediaClient;

    @Override
    public void addLinks(EntityModel<Customer> resource) {
        Customer customer = resource.getContent();

        try {
            Link walletLink = hypermediaClient.traverseWalletApi(traverson -> {
                Map<String, Object> parameters = new HashMap<>();
                parameters.put("id", resource.getContent().getId());
                return traverson
                        .follow("wallet:customer")
                        .withTemplateParameters(parameters)
                        .asLink();
            });

            resource.add(walletLink);
        } catch (ResourceAccessException e) {
            logger.warn(e.toString());
        }

        resource.add(linkTo(methodOn(CustomerController.class)
                .getCustomer(customer.getId())).withSelfRel()
                .andAffordance(afford(methodOn(CustomerController.class)
                        .updateCustomer(customer.getId(), null)))
                .andAffordance(afford(methodOn(CustomerController.class)
                        .deleteCustomer(customer.getId())))
        );
    }

    @Override
    public void addLinks(CollectionModel<EntityModel<Customer>> resources) {

    }
}
