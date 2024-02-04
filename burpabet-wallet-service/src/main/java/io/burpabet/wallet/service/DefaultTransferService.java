package io.burpabet.wallet.service;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;

import io.burpabet.common.annotations.ControlService;
import io.burpabet.common.annotations.TransactionMandatory;
import io.burpabet.common.util.Money;
import io.burpabet.wallet.model.Account;
import io.burpabet.wallet.model.Transaction;
import io.burpabet.wallet.repository.AccountRepository;
import io.burpabet.wallet.repository.TransactionItemRepository;
import io.burpabet.wallet.repository.TransactionRepository;

@ControlService
public class DefaultTransferService implements TransferService {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private TransactionItemRepository transactionItemRepository;

    @Override
    public void deleteAllInBatch() {
        transactionItemRepository.deleteAllInBatch();
        transactionRepository.deleteAllInBatch();
    }

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

        // Lookup accounts with locks
        final List<Account> accounts = accountRepository.findAllByIdForUpdate(legs.keySet());

        final Transaction.Builder transactionBuilder = Transaction.builder()
                .withId(request.getId())
                .withJurisdiction(request.getJurisdiction())
                .withTransferType(request.getTransactionType())
                .withBookingDate(request.getBookingDate())
                .withTransferDate(request.getTransferDate());

        legs.forEach((accountId, value) -> {
            final Money amount = value.getFirst();

            Account account = accounts.stream()
                    .filter(a -> Objects.equals(a.getId(), accountId))
                    .findFirst()
                    .orElseThrow(() -> new NoSuchAccountException(accountId));

            transactionBuilder
                    .andItem()
                    .withAccount(account)
                    .withRunningBalance(account.getBalance())
                    .withAmount(amount)
                    .withNote(value.getSecond())
                    .then();

            account.addAmount(amount);
        });

        Transaction transaction = transactionBuilder.build();
        transactionItemRepository.saveAll(transaction.getItems());

        return transactionRepository.save(transaction);
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
