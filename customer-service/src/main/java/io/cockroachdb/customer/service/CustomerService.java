package io.cockroachdb.customer.service;

import io.cockroachdb.betting.common.annotations.OutboxOperation;
import io.cockroachdb.betting.common.annotations.Retryable;
import io.cockroachdb.betting.common.annotations.SagaCoordinator;
import io.cockroachdb.betting.common.annotations.ServiceFacade;
import io.cockroachdb.betting.common.annotations.TimeTravel;
import io.cockroachdb.betting.common.annotations.TimeTravelMode;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;
import io.cockroachdb.betting.common.domain.EventType;
import io.cockroachdb.betting.common.domain.Registration;
import io.cockroachdb.betting.common.domain.RegistrationEvent;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.betting.common.outbox.OutboxRepository;
import io.cockroachdb.betting.common.shell.DebugSupport;
import io.cockroachdb.customer.model.Customer;
import io.cockroachdb.customer.repository.CustomerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.util.Pair;

import java.util.Optional;

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
    public Customer findByEmail(String email) {
        return customerRepository.findByEmail(email).orElseThrow(() -> new NoSuchCustomerException(email));
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
