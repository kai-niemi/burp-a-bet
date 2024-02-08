package io.burpabet.wallet.web;

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

import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.wallet.model.Account;
import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;
import io.burpabet.wallet.repository.AccountRepository;
import io.burpabet.wallet.repository.CustomerAccountRepository;
import io.burpabet.wallet.repository.OperatorAccountRepository;
import io.burpabet.wallet.service.NoSuchAccountException;

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
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
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
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<EntityModel<OperatorAccount>> getOperatorAccount(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(operatorAccountResourceAssembler.toModel(
                operatorAccountRepository.findById(id).orElseThrow(() -> new NoSuchAccountException(id))));
    }

    @GetMapping(value = "/customer")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<PagedModel<EntityModel<CustomerAccount>>> findAllCustomerAccounts(
            @PageableDefault(size = 15) Pageable page) {
        Page<CustomerAccount> accountPage = customerAccountRepository.findAll(page);
        return ResponseEntity.ok(customerAccountPagedResourcesAssembler
                .toModel(accountPage, customerAccountResourceAssembler));
    }

    @GetMapping(value = "/customer/{id}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public HttpEntity<EntityModel<CustomerAccount>> getCustomerAccount(@PathVariable("id") UUID id) {
        return ResponseEntity.ok(customerAccountResourceAssembler.toModel(
                customerAccountRepository.findById(id).orElseThrow(() -> new NoSuchAccountException(id))));
    }

    @PatchMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public ResponseEntity<?> updateAccount(@PathVariable("id") UUID id, @RequestBody Account account) {
        Account accountProxy = accountRepository.getReferenceById(id);
        accountProxy.setClosed(account.isClosed());
        accountProxy.setDescription(account.getDescription());
        accountProxy.setName(account.getName());
        accountRepository.save(account);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/{id}")
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public ResponseEntity<Void> deleteAccount(@PathVariable("id") UUID id) {
        accountRepository.deleteById(id);
        return ResponseEntity.ok().build();
    }
}
