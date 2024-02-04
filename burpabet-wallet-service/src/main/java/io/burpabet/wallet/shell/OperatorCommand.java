package io.burpabet.wallet.shell;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.outbox.OutboxRepository;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;
import io.burpabet.common.shell.JurisdictionValueProvider;
import io.burpabet.common.util.Money;
import io.burpabet.common.util.RandomData;
import io.burpabet.wallet.model.AccountType;
import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;
import io.burpabet.wallet.service.AccountService;
import io.burpabet.wallet.service.NoSuchAccountException;
import io.burpabet.wallet.service.TransferService;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class OperatorCommand extends AbstractShellComponent {
    private static final AtomicInteger counter = new AtomicInteger();

    @Autowired
    private OutboxRepository outboxRepository;

    @Autowired
    private AccountService accountService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private AnsiConsole ansiConsole;

    @Value("${server.port}")
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @ShellMethod(value = "Reset all account data", key = {"t", "reset"})
    @TransactionBoundary
    public void reset() {
        transferService.deleteAllInBatch();
        accountService.deleteAllInBatch();
        outboxRepository.deleteAllInBatch();

//        jdbcTemplate.execute("delete from flyway_schema_history where 1=1");
    }

    @ShellMethod(value = "Create a set of operator accounts (optional)", key = {"co", "create-operators"})
    @TransactionBoundary
    public void createOperators(
            @ShellOption(help = "initial account balance (can go negative)", defaultValue = "0.00") String balance,
            @ShellOption(help = "account currency", defaultValue = "USD") String currency,
            @ShellOption(help = "number of accounts per jurisdiction", defaultValue = "10") int count,
            @ShellOption(help = "operator jurisdiction (all if omitted)", defaultValue = ShellOption.NULL,
                    valueProvider = JurisdictionValueProvider.class) String jurisdiction
    ) {
        EnumSet<Jurisdiction> jurisdictions;
        if (jurisdiction == null) {
            jurisdictions = EnumSet.allOf(Jurisdiction.class);
        } else {
            jurisdictions = EnumSet.of(Jurisdiction.valueOf(jurisdiction));
        }

        jurisdictions
                .parallelStream()
                .forEach(j -> accountService.createOperatorAccounts(count,
                () -> OperatorAccount.builder()
                        .withJurisdiction(j.name())
                        .withBalance(Money.of(balance, currency))
                        .withName("operator-" + j.name() + "-" + counter.incrementAndGet()) // not unique
                        .withDescription(RandomData.randomRoachFact())
                        .withAccountType(AccountType.LIABILITY)
                        .withAllowNegative(true) // allowed for operators
                        .build(), operatorAccount -> {
                    ansiConsole.yellow("Created '%s' in %s"
                            .formatted(operatorAccount.getName(), operatorAccount.getJurisdiction())).nl();
                }));
    }

    @ShellMethod(value = "Create a set of customer accounts (optional)", key = {"cc", "create-customers"})
    @TransactionBoundary
    public void createCustomers(
            @ShellOption(help = "initial account balance (can go negative)", defaultValue = "0.00") String balance,
            @ShellOption(help = "account currency", defaultValue = "USD") String currency,
            @ShellOption(help = "number of accounts per jurisdiction", defaultValue = "10") int count,
            @ShellOption(help = "operator jurisdiction (if picked by random)", defaultValue = "SE",
                    valueProvider = JurisdictionValueProvider.class) String jurisdiction,
            @ShellOption(help = "operator id (random if omitted)", defaultValue = ShellOption.NULL) String operator
    ) {
        OperatorAccount operatorAccount;

        if (Objects.nonNull(operator)) {
            UUID id = UUID.fromString(operator);
            operatorAccount = accountService.findOperatorAccountById(id)
                    .orElseThrow(() -> new NoSuchAccountException(id));
        } else {
            operatorAccount = accountService.findOperatorAccounts(jurisdiction)
                    .stream()
                    .findAny()
                    .orElseThrow(() -> new OperatorsNotFoundException(
                            "No operators found for jurisdiction: " + jurisdiction));
        }

        accountService.createCustomerAccounts(count,
                () -> CustomerAccount.builder()
                        .withJurisdiction(operatorAccount.getJurisdiction())
                        .withOperatorId(operatorAccount.getId())
                        .withBalance(Money.of(balance, currency))
                        .withName(RandomData.randomFullName())
                        .withDescription(RandomData.randomRoachFact())
                        .withAccountType(AccountType.ASSET)
                        .withAllowNegative(false)
                        .build(), customerAccount -> {
                    ansiConsole.yellow("Created '%s' in %s"
                            .formatted(customerAccount.getName(), customerAccount.getJurisdiction())).nl();
                });
    }

    @ShellMethod(value = "Print and API index url", key = {"u", "url"})
    public void url() throws IOException {
        String uri = ServletUriComponentsBuilder.newInstance()
                .scheme("http")
                .host(InetAddress.getLocalHost().getHostAddress())
                .port(port)
                .build()
                .toUriString();

        ansiConsole.cyan(uri).nl();
    }

    @ShellMethod(value = "Print random fact", key = {"f", "fact"})
    public void fact() {
        ansiConsole.cyan(RandomData.randomRoachFact()).nl();
    }
}
