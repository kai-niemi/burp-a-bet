package io.cockroachdb.wallet.service;

import io.cockroachdb.betting.common.annotations.ControlService;
import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.wallet.model.CustomerAccount;
import io.cockroachdb.wallet.model.OperatorAccount;
import io.cockroachdb.wallet.repository.CustomerAccountRepository;
import io.cockroachdb.wallet.repository.OperatorAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ControlService
public class DefaultAccountService implements AccountService {
    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private OperatorAccountRepository operatorAccountRepository;

    @Override
    public CustomerAccount createCustomerAccount(CustomerAccount account) {
        // Idempotency check by foreign id (customer id)
        Optional<CustomerAccount> optional = customerAccountRepository.findByForeignId(account.getForeignId());
        return optional.orElseGet(() -> customerAccountRepository.save(account));
    }

    @Override
    public OperatorAccount createOperatorAccount(OperatorAccount account) {
        return operatorAccountRepository.save(account);
    }


    @Override
    public Optional<CustomerAccount> findCustomerAccountByForeignId(UUID foreignId) {
        return customerAccountRepository.findByForeignId(foreignId);
    }

    @Override
    public List<OperatorAccount> findOperatorAccountsByJurisdiction(Jurisdiction jurisdiction) {
        return operatorAccountRepository.findAllAccountsByJurisdiction(jurisdiction);
    }

    @Override
    public Optional<OperatorAccount> findOperatorAccountById(UUID id) {
        return operatorAccountRepository.findById(id);
    }
}
