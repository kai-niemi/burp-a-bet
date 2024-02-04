package io.burpabet.customer.web;

import io.burpabet.customer.model.Customer;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.server.SimpleRepresentationModelAssembler;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.*;

@Component
public class CustomerResourceAssembler implements SimpleRepresentationModelAssembler<Customer> {
    @Override
    public void addLinks(EntityModel<Customer> resource) {
        Customer customer = resource.getContent();
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
