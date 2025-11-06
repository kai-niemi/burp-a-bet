package io.cockroachdb.wallet.service;

import io.cockroachdb.betting.common.annotations.Retryable;
import io.cockroachdb.betting.common.annotations.ServiceFacade;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;
import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.outbox.OutboxRepository;
import io.cockroachdb.betting.common.util.Money;
import io.cockroachdb.wallet.model.CustomerAccount;
import io.cockroachdb.wallet.model.OperatorAccount;
import io.cockroachdb.wallet.repository.CustomerAccountRepository;
import io.cockroachdb.wallet.repository.OperatorAccountRepository;
import io.cockroachdb.wallet.repository.TransactionItemRepository;
import io.cockroachdb.wallet.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@ServiceFacade
public class BatchService {
    @Autowired
    private TransferService transferService;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private OperatorAccountRepository operatorAccountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionItemRepository transactionItemRepository;

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @TransactionBoundary
    public void deleteAllInBatch() {
        transactionItemRepository.deleteAllInBatch();
        transactionRepository.deleteAllInBatch();
        customerAccountRepository.deleteAllInBatch();
        operatorAccountRepository.deleteAllInBatch();
        outboxRepository.deleteAllInBatch();
    }

    @TransactionBoundary
    public Optional<OperatorAccount> findOperatorAccountById(UUID id) {
        return operatorAccountRepository.findById(id);
    }

    @TransactionBoundary
    public List<OperatorAccount> findOperatorAccounts(Jurisdiction jurisdiction) {
        return operatorAccountRepository.findAllAccountsByJurisdiction(jurisdiction);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createOperatorAccounts(int numAccounts, Supplier<OperatorAccount> supplier,
                                       Consumer<OperatorAccount> consumer) {
        List<OperatorAccount> batch = new ArrayList<>();

        IntStream.range(0, numAccounts)
                .forEach(value -> {
                    batch.add(supplier.get());
                    if (batch.size() > 256) {
                        transactionTemplate.executeWithoutResult(
                                transactionStatus -> operatorAccountRepository.saveAllAndFlush(batch));
                        batch.forEach(consumer);
                        batch.clear();
                    }
                });

        if (!batch.isEmpty()) {
            transactionTemplate.executeWithoutResult(
                    transactionStatus -> operatorAccountRepository.saveAllAndFlush(batch));
            batch.forEach(consumer);
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void createCustomerAccounts(int numAccounts, Supplier<CustomerAccount> supplier,
                                       Consumer<CustomerAccount> consumer) {
        List<CustomerAccount> batch = new ArrayList<>();

        IntStream.range(0, numAccounts)
                .forEach(value -> {
                    batch.add(supplier.get());
                    if (batch.size() > 256) {
                        transactionTemplate.executeWithoutResult(
                                transactionStatus -> customerAccountRepository.saveAllAndFlush(batch));
                        batch.forEach(consumer);
                        batch.clear();
                    }
                });

        if (!batch.isEmpty()) {
            transactionTemplate.executeWithoutResult(
                    transactionStatus -> customerAccountRepository.saveAllAndFlush(batch));
            batch.forEach(consumer);
        }
    }

    @TransactionBoundary
    @Retryable
    public Money grantBonus(OperatorAccount operatorAccount, Money grant) {
        TransferRequest.Builder requestBuilder = TransferRequest.builder()
                .withId(UUID.randomUUID())
                .withJurisdiction(operatorAccount.getJurisdiction())
                .withTransactionType("extra-bonus")
                .withBookingDate(LocalDate.now());

        AtomicReference<Money> total = new AtomicReference<>(Money.zero(grant.getCurrency()));

        customerAccountRepository.findAllByOperatorId(operatorAccount.getId())
                .forEach(customerAccount -> {
                    requestBuilder
                            .addLeg()
                            .withId(customerAccount.getId())
                            .withAmount(grant)
                            .withNote("Bonus from " + operatorAccount.getName())
                            .then()
                            .addLeg()
                            .withId(operatorAccount.getId())
                            .withAmount(grant.negate())
                            .withNote("Bonus grant to " + customerAccount.getName())
                            .then();
                    total.set(total.get().plus(grant));
                });

        if (total.get().isGreaterThan(Money.zero(grant.getCurrency()))) {
            transferService.submitTransferRequest(requestBuilder.build());
        }

        return total.get();
    }
}
