package io.burpabet.customer.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.domain.Status;
import io.burpabet.customer.model.Customer;
import io.burpabet.customer.repository.CustomerRepository;
import io.burpabet.customer.service.NoSuchCustomerException;

@RestController
@RequestMapping(path = "/api/customer")
public class CustomerController {
    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private CustomerResourceAssembler customerResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<Customer> customerPagedResourcesAssembler;

    @GetMapping(value = "/")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<PagedModel<EntityModel<Customer>>> findAll(
            @PageableDefault(size = 15) Pageable page) {
        Page<Customer> customerPage = customerRepository.findAll(page);
        return ResponseEntity.ok(customerPagedResourcesAssembler.toModel(customerPage, customerResourceAssembler));
    }

    @GetMapping(value = "/jurisdiction/{jurisdiction}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<PagedModel<EntityModel<Customer>>> findAllWithJurisdiction(
            @PathVariable("jurisdiction") Jurisdiction jurisdiction,
            @PageableDefault(size = 15) Pageable page) {
        Page<Customer> customerPage = customerRepository.findAllWithJurisdiction(jurisdiction, page);
        return ResponseEntity.ok(customerPagedResourcesAssembler.toModel(customerPage, customerResourceAssembler));
    }

    @GetMapping(value = "/status/{status}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<PagedModel<EntityModel<Customer>>> findAllWithStatus(
            @PathVariable("status") Status status,
            @PageableDefault(size = 15) Pageable page) {
        Page<Customer> customerPage = customerRepository.findAllWithStatus(status, page);
        return ResponseEntity.ok(customerPagedResourcesAssembler.toModel(customerPage, customerResourceAssembler));
    }

    @GetMapping(value = "/any")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<EntityModel<Customer>> findAnyCustomer() {
        return ResponseEntity.ok(customerResourceAssembler
                .toModel(customerRepository.findAny()
                        .orElseThrow(() -> new NoSuchCustomerException("No customers"))));
    }

    @GetMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<EntityModel<Customer>> getCustomer(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(customerResourceAssembler
                .toModel(customerRepository.findById(id)
                        .orElseThrow(() -> new NoSuchCustomerException(id.toString()))));
    }

    @PatchMapping(value = "/{id}")
    @TransactionBoundary
    public ResponseEntity<?> updateCustomer(@PathVariable("id") UUID id,
                                            @RequestBody Customer customer) {
        Customer customerProxy = customerRepository.getReferenceById(id);
        customerProxy.setEmail(customer.getEmail());
        customerProxy.setName(customer.getName());
        customerRepository.save(customer);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{id}")
    @TransactionBoundary
    public ResponseEntity<Void> deleteCustomer(@PathVariable("id") UUID id) {
        customerRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
