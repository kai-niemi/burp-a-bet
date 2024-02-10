package io.burpabet.customer.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import io.burpabet.common.annotations.OutboxOperation;
import io.burpabet.common.annotations.Retryable;
import io.burpabet.common.annotations.SagaCoordinator;
import io.burpabet.common.annotations.ServiceFacade;
import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.EventType;
import io.burpabet.common.domain.Registration;
import io.burpabet.common.domain.RegistrationEvent;
import io.burpabet.common.domain.Status;
import io.burpabet.common.outbox.OutboxRepository;
import io.burpabet.common.shell.DebugSupport;
import io.burpabet.customer.model.Customer;
import io.burpabet.customer.repository.CustomerRepository;

@ServiceFacade
@SagaCoordinator("registration")
public class CustomerService {
    public static Registration toRegistration(Customer entity) {
        Registration registration = new Registration();
        registration.setEventId(entity.getId());
        registration.setEntityId(entity.getId());
        registration.setStatus(entity.getStatus());

        registration.setEmail(entity.getEmail());
        registration.setName(entity.getName());
        registration.setJurisdiction(entity.getJurisdiction());
        registration.setOperatorId(entity.getOperatorId());

        return registration;
    }

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @TransactionBoundary
    public void deleteAllInBatch() {
        customerRepository.deleteAllInBatch();
        outboxRepository.deleteAllInBatch();
    }

    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public Page<Customer> findAll(Pageable pageable) {
        return customerRepository.findAll(pageable);
    }

    @TransactionBoundary
    @Retryable
    @OutboxOperation(aggregateType = "registration")
    public Registration registerCustomer(Customer customer) {
        customer = customerRepository.save(customer);
        return toRegistration(customer);
    }

    @TransactionBoundary
    @Retryable
    public RegistrationEvent confirmRegistration(RegistrationEvent fromWallet, RegistrationEvent fromBetting) {
        Registration walletPayload = fromWallet.getPayload();
        Registration bettingPayload = fromBetting.getPayload();

        Optional<Customer> optional = customerRepository.findById(walletPayload.getEntityId());
        if (optional.isPresent()) {
            Customer customer = optional.get();

            String origin = null;

            if (walletPayload.getStatus().equals(Status.APPROVED) &&
                    bettingPayload.getStatus().equals(Status.APPROVED)) {
                customer.setStatus(Status.APPROVED);
            } else if (walletPayload.getStatus().equals(Status.REJECTED) &&
                    bettingPayload.getStatus().equals(Status.REJECTED)) {
                customer.setStatus(Status.REJECTED);
            } else if (walletPayload.getStatus().equals(Status.REJECTED) ||
                    bettingPayload.getStatus().equals(Status.REJECTED)) {
                customer.setStatus(Status.ROLLBACK);
                origin = walletPayload.getStatus().equals(Status.REJECTED)
                        ? walletPayload.getOrigin() : bettingPayload.getOrigin();
            }

            customer.setOperatorId(walletPayload.getOperatorId());

            DebugSupport.logJourneyCompletion(logger,
                    "Registration",
                    Pair.of("Wallet", walletPayload),
                    Pair.of("Betting", bettingPayload),
                    customer.getStatus());

            Registration registration = toRegistration(customer);
            registration.setOrigin(origin);

            return new RegistrationEvent(fromWallet.getEventId(), EventType.insert, registration);
        } else {
            logger.warn("Missing customer with id: {}", walletPayload.getEntityId());
            return null;
        }
    }
}
