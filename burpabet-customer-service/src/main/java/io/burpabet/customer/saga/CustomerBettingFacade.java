package io.burpabet.customer.saga;

import io.burpabet.common.annotations.OutboxOperation;
import io.burpabet.common.annotations.Retryable;
import io.burpabet.common.annotations.ServiceFacade;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.BetPlacement;
import io.burpabet.common.domain.BetSettlement;
import io.burpabet.common.domain.Status;
import io.burpabet.common.util.Money;
import io.burpabet.customer.model.Customer;
import io.burpabet.customer.repository.CustomerRepository;
import io.burpabet.customer.service.SimpleSpendingLimit;
import io.burpabet.customer.service.SpendingLimit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@ServiceFacade
public class CustomerBettingFacade {
    private final Map<UUID, SpendingLimit> customerSpendingLimits = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerRepository customerRepository;

    @TransactionBoundary
    @Retryable
    @OutboxOperation(aggregateType = "placement")
    public BetPlacement acquireSpendingCredits(BetPlacement placement) {
        Optional<Customer> optional = customerRepository.findById(placement.getCustomerId());

        placement.setOrigin("customer-service");

        if (optional.isEmpty()) {
            placement.setStatus(Status.REJECTED);
            placement.setStatusDetail("No such customer: " + placement.getCustomerId());
            logger.warn("Bet placement rejected (no customer account): {}", placement);
            return placement;
        }

        Customer customer = optional.get();

        if (!Status.APPROVED.equals(customer.getStatus())) {
            placement.setStatus(Status.REJECTED);
            placement.setStatusDetail("Customer not approved (" + customer.getStatus() + ")");
            logger.warn("Bet placement rejected (customer not approved): {}", placement);
            return placement;
        }

        final Money wager = placement.getStake();

        boolean permitted = customerSpendingLimits.computeIfAbsent(placement.getCustomerId(),
                        x -> new SimpleSpendingLimit(Money.of("50.00", wager.getCurrency()),
                                Duration.ofSeconds(60)))
                .acquirePermission(placement.getStake());
        if (permitted) {
            placement.setStatus(Status.APPROVED);
            placement.setStatusDetail("Within spending budget");

            logger.info("Bet placement approved (in spending budget): {}", placement);
        } else {
            placement.setStatus(Status.REJECTED);
            placement.setStatusDetail("Exhausted spending budget of $50/min");

            logger.warn("Bet placement rejected (exhausted spending budget): {}", placement);
        }

        placement.setCustomerName(customer.getName());
        placement.setJurisdiction(customer.getJurisdiction());

        return placement;
    }

    @TransactionBoundary
    @Retryable
    public void releaseSpendingCredits(BetPlacement placement) {
        customerSpendingLimits.computeIfPresent(placement.getCustomerId(),
                (id, spendingLimit) -> {
                    spendingLimit.releasePermission(placement.getStake());
                    logger.info("Bet placement reversal (released spending credits); {}", placement);
                    return null;
                });
    }

    @TransactionBoundary
    @Retryable
    @OutboxOperation(aggregateType = "settlement")
    public BetSettlement approveSettlement(BetSettlement settlement) {
        settlement.setStatus(Status.APPROVED);
        settlement.setOrigin("customer-service");
        logger.info("Bet settlement approved: {}", settlement);
        return settlement;
    }
}
