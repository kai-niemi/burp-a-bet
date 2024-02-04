package io.burpabet.wallet.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;

import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;

public interface AccountService {
    void deleteAllInBatch();

    CustomerAccount createCustomerAccount(CustomerAccount account);

    OperatorAccount createOperatorAccount(OperatorAccount account);

    void createOperatorAccounts(int numAccounts, Supplier<OperatorAccount> supplier,
                                Consumer<OperatorAccount> consumer);

    void createCustomerAccounts(int numAccounts, Supplier<CustomerAccount> supplier,
                                Consumer<CustomerAccount> consumer);

    Optional<CustomerAccount> findCustomerAccountByForeignId(UUID foreignId);

    Optional<OperatorAccount> findOperatorAccountById(UUID id);

    List<OperatorAccount> findOperatorAccounts(String jurisdiction);
}
