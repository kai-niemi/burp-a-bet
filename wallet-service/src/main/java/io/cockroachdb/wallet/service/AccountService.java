package io.cockroachdb.wallet.service;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.wallet.model.CustomerAccount;
import io.cockroachdb.wallet.model.OperatorAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountService {
    CustomerAccount createCustomerAccount(CustomerAccount account);

    OperatorAccount createOperatorAccount(OperatorAccount account);

    Optional<OperatorAccount> findOperatorAccountById(UUID id);

    List<OperatorAccount> findOperatorAccountsByJurisdiction(Jurisdiction jurisdiction);

    Optional<CustomerAccount> findCustomerAccountByForeignId(UUID foreignId);
}
