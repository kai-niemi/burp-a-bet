package io.burpabet.wallet.service;

import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;

import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    CustomerAccount createCustomerAccount(CustomerAccount account);

    OperatorAccount createOperatorAccount(OperatorAccount account);

    Optional<OperatorAccount> findOperatorAccountById(UUID id);

    Optional<CustomerAccount> findCustomerAccountByForeignId(UUID foreignId);
}
