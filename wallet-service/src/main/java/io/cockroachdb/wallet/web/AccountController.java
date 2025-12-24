package io.cockroachdb.wallet.web;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.cockroachdb.betting.common.annotations.FollowerRead;
import io.cockroachdb.betting.common.annotations.FollowerReadMode;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;
import io.cockroachdb.wallet.model.Account;
import io.cockroachdb.wallet.model.CustomerAccount;
import io.cockroachdb.wallet.model.OperatorAccount;
import io.cockroachdb.wallet.repository.AccountRepository;
import io.cockroachdb.wallet.repository.CustomerAccountRepository;
import io.cockroachdb.wallet.repository.OperatorAccountRepository;
import io.cockroachdb.wallet.service.NoSuchAccountException;

@RestController
@RequestMapping(path = "/api/account")
public class AccountController {
    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private CustomerAccountRepository customerAccountRepository;

    @Autowired
    private CustomerAccountResourceAssembler customerAccountResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<CustomerAccount> customerAccountPagedResourcesAssembler;

    @Autowired
    private OperatorAccountRepository operatorAccountRepository;

    @Autowired
    private OperatorAccountResourceAssembler operatorAccountResourceAssembler;

    @Autowired
    private PagedResourcesAssembler<OperatorAccount> operatorAccountPagedResourcesAssembler;

    @GetMapping(value = "/operator")
    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public HttpEntity<PagedModel<EntityModel<OperatorAccount>>> findAllOperatorAccounts(
            @PageableDefault(size = 15) Pageable page,
            @RequestParam(required = false, name = "shuffle", defaultValue = "false") boolean shuffle) {
        Page<OperatorAccount> accountPage = shuffle
                ? operatorAccountRepository.findAllByRandom(page)
                : operatorAccountRepository.findAll(page);
        return ResponseEntity.ok(operatorAccountPagedResourcesAssembler
                .toModel(accountPage, operatorAccountResourceAssembler));
    }

    @GetMapping(value = "/operator/{id}")
    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public HttpEntity<EntityModel<OperatorAccount>> getOperatorAccount(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(operatorAccountResourceAssembler.toModel(
                operatorAccountRepository.findById(id).orElseThrow(() -> new NoSuchAccountException(id))));
    }

    @GetMapping(value = "/customer")
    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public HttpEntity<PagedModel<EntityModel<CustomerAccount>>> findAllCustomerAccounts(
            @PageableDefault(size = 15) Pageable page) {
        Page<CustomerAccount> accountPage = customerAccountRepository.findAll(page);
        return ResponseEntity.ok(customerAccountPagedResourcesAssembler
                .toModel(accountPage, customerAccountResourceAssembler));
    }

    @GetMapping(value = "/customer/{id}")
    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public HttpEntity<EntityModel<CustomerAccount>> getCustomerAccount(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(customerAccountResourceAssembler.toModel(
                customerAccountRepository.findById(id).orElseThrow(() -> new NoSuchAccountException(id))));
    }

    @GetMapping(value = "/customer/foreign/{id}")
    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public HttpEntity<EntityModel<CustomerAccount>> getCustomerAccountByForeignId(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(customerAccountResourceAssembler.toModel(
                customerAccountRepository.findByForeignId(id).orElseThrow(() -> new NoSuchAccountException(id))));
    }

    @PatchMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public ResponseEntity<?> updateAccount(@PathVariable("id") UUID id, @RequestBody Account account) {
        Account accountProxy = accountRepository.getReferenceById(id);
        accountProxy.setClosed(account.isClosed());
        accountProxy.setDescription(account.getDescription());
        accountProxy.setName(account.getName());
        accountRepository.save(account);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @FollowerRead(mode = FollowerReadMode.EXACT_STALENESS_READ))
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") UUID id) {
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
