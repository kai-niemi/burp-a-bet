package io.burpabet.wallet.service;

import io.burpabet.common.annotations.ControlService;
import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;
import io.burpabet.wallet.repository.CustomerAccountRepository;
import io.burpabet.wallet.repository.OperatorAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;

@ControlService
public class DefaultAccountService implements AccountService {
    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private OperatorAccountRepository operatorAccountRepository;

    @Override
    public void deleteAllInBatch() {
        customerAccountRepository.deleteAllInBatch();
        operatorAccountRepository.deleteAllInBatch();
    }

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
    public void createOperatorAccounts(int numAccounts, Supplier<OperatorAccount> supplier,
                                       Consumer<OperatorAccount> consumer) {
        List<OperatorAccount> batch = new ArrayList<>();
        IntStream.range(0, numAccounts).forEach(
                i -> batch.add(supplier.get()));
        operatorAccountRepository.saveAllAndFlush(batch);
        batch.forEach(consumer);
    }

    @Override
    public void createCustomerAccounts(int numAccounts, Supplier<CustomerAccount> supplier,
                                       Consumer<CustomerAccount> consumer) {
        List<CustomerAccount> batch = new ArrayList<>();
        IntStream.range(0, numAccounts).forEach(
                i -> batch.add(supplier.get()));
        customerAccountRepository.saveAllAndFlush(batch);
        batch.forEach(consumer);
    }

    @Override
    public Optional<CustomerAccount> findCustomerAccountByForeignId(UUID foreignId) {
        return customerAccountRepository.findByForeignId(foreignId);
    }

    @Override
    public Optional<OperatorAccount> findOperatorAccountById(UUID id) {
        return operatorAccountRepository.findById(id);
    }

    @Override
    public List<OperatorAccount> findOperatorAccounts(String jurisdiction) {
        return operatorAccountRepository.findAllAccountsByJurisdiction(jurisdiction);
    }
}
