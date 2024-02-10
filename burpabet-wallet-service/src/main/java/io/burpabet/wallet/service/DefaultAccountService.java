package io.burpabet.wallet.service;

import io.burpabet.common.annotations.ControlService;
import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;
import io.burpabet.wallet.repository.CustomerAccountRepository;
import io.burpabet.wallet.repository.OperatorAccountRepository;
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
