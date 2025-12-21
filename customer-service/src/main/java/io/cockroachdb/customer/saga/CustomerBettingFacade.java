package io.cockroachdb.customer.saga;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.Currency;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.cockroachdb.betting.common.annotations.OutboxOperation;
import io.cockroachdb.betting.common.annotations.ServiceFacade;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;
import io.cockroachdb.betting.common.domain.BetPlacement;
import io.cockroachdb.betting.common.domain.BetSettlement;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.betting.common.util.Money;
import io.cockroachdb.customer.model.Customer;
import io.cockroachdb.customer.repository.CustomerRepository;
import io.cockroachdb.customer.service.SimpleSpendingLimit;
import io.cockroachdb.customer.service.SpendingLimit;

@ServiceFacade
public class CustomerBettingFacade {
    private boolean spendingLimitsEnabled = true;

    private final Map<UUID, SpendingLimit> customerSpendingLimits = new HashMap<>();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Transient (in-memory) spending limits for simplicity.
     */
    private SpendingLimit createSpendingLimit(BigDecimal budget, Currency currency) {
        if (!spendingLimitsEnabled || budget == null) {
            return new UnlimitedSpendingLimit();
        }
        return new SimpleSpendingLimit(Money.of(budget, currency), Duration.ofMinutes(1));
    }

    public void toggleSpendingLimits() {
        this.spendingLimitsEnabled = !this.spendingLimitsEnabled;
        this.customerSpendingLimits.clear();
        logger.info("Spending limits %s".formatted(spendingLimitsEnabled ? "enabled" : "disabled"));
    }

    @TransactionBoundary
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

        SpendingLimit spendingLimit = customerSpendingLimits.computeIfAbsent(placement.getCustomerId(),
                x -> createSpendingLimit(customer.getSpendingBudgetPerMinute(), wager.getCurrency()));

        if (spendingLimit.acquirePermission(placement.getStake())) {
            placement.setStatus(Status.APPROVED);
            placement.setStatusDetail("Within spending budget: " + spendingLimit.description());

            logger.info("Bet placement approved (in spending budget): {}", placement);
        } else {
            placement.setStatus(Status.REJECTED);
            placement.setStatusDetail("Exhausted spending budget: " + spendingLimit.description());

            logger.warn("Bet placement rejected (exhausted spending budget): {}", placement);
        }

        placement.setCustomerName(customer.getName());
        placement.setJurisdiction(customer.getJurisdiction());

        return placement;
    }

    @TransactionBoundary
    public void releaseSpendingCredits(BetPlacement placement) {
        customerSpendingLimits.computeIfPresent(placement.getCustomerId(),
                (id, spendingLimit) -> {
                    spendingLimit.releasePermission(placement.getStake());
                    logger.info("Bet placement reversal (released spending credits); {}", placement);
                    return null;
                });
    }

    @TransactionBoundary
    @OutboxOperation(aggregateType = "settlement")
    public BetSettlement approveSettlement(BetSettlement settlement) {
        settlement.setStatus(Status.APPROVED);
        settlement.setOrigin("customer-service");
        logger.info("Bet settlement approved: {}", settlement);
        return settlement;
    }
}
