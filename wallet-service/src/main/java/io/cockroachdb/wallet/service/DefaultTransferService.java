package io.cockroachdb.wallet.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.util.Assert;

import io.cockroachdb.betting.common.annotations.ControlService;
import io.cockroachdb.betting.common.annotations.TransactionMandatory;
import io.cockroachdb.betting.common.util.Money;
import io.cockroachdb.wallet.model.Account;
import io.cockroachdb.wallet.model.Transaction;
import io.cockroachdb.wallet.model.TransactionItem;
import io.cockroachdb.wallet.repository.AccountRepository;
import io.cockroachdb.wallet.repository.TransactionItemRepository;
import io.cockroachdb.wallet.repository.TransactionRepository;

@ControlService
public class DefaultTransferService implements TransferService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionItemRepository transactionItemRepository;

    @Override
    @TransactionMandatory
    public Transaction submitTransferRequest(TransferRequest request) {
        // Idempotency check
        Optional<Transaction> transactionCheck = transactionRepository.findById(request.getId());
        if (transactionCheck.isPresent()) {
            return transactionCheck.get();
        }

        if (request.getAccountLegs().size() < 2) {
            throw new BadRequestException("Must have at least two account legs");
        }

        // Coalesce multi-legged transactions
        final Map<UUID, Pair<Money, String>> legs = coalesce(request);

        final Transaction transaction = transactionRepository.saveAndFlush(Transaction.builder()
                .withJurisdiction(request.getJurisdiction())
                .withTransferType(request.getTransactionType())
                .withBookingDate(request.getBookingDate())
                .withTransferDate(request.getTransferDate())
                .build());

        Assert.notNull(transaction.getId(), "id is null");

        final List<TransactionItem> transactionItems = new ArrayList<>();

        // Lookup accounts with pessimistic locks
        final List<Account> accounts = accountRepository.findAllByIdForUpdate(legs.keySet());

        legs.forEach((accountId, value) -> {
            Account account = accounts.stream()
                    .filter(a -> Objects.equals(a.getId(), accountId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchAccountException(accountId));

            final Money amount = value.getFirst();

            transactionItems.add(TransactionItem.builder()
                    .withTransaction(transaction)
                    .withAccount(account)
                    .withRunningBalance(account.getBalance())
                    .withAmount(amount)
                    .withNote(value.getSecond())
                    .withJurisdiction(request.getJurisdiction())
                    .build());

            account.addAmount(amount);
        });

        transactionItemRepository.saveAll(transactionItems);

        transaction.addItems(transactionItems);

        return transaction;
    }

    private Map<UUID, Pair<Money, String>> coalesce(TransferRequest request) {
        final Map<UUID, Pair<Money, String>> legs = new HashMap<>();
        final Map<Currency, BigDecimal> amounts = new HashMap<>();

        // Compact accounts and verify that the total balance for the legs with the same currency is zero
        request.getAccountLegs().forEach(leg -> {
            legs.compute(leg.getId(),
                    (key, amount) -> (amount == null)
                            ? Pair.of(leg.getAmount(), leg.getNote())
                            : Pair.of(amount.getFirst().plus(leg.getAmount()), leg.getNote()));
            amounts.compute(leg.getAmount().getCurrency(),
                    (currency, amount) -> (amount == null)
                            ? leg.getAmount().getAmount() : leg.getAmount().getAmount().add(amount));
        });

        // The sum of debits for all accounts must equal the corresponding sum of credits (per currency)
        amounts.forEach((key, value) -> {
            if (value.compareTo(BigDecimal.ZERO) != 0) {
                throw new BadRequestException("Unbalanced transaction: currency ["
                                              + key + "], amount sum [" + value + "]");
            }
        });

        return legs;
    }
}
