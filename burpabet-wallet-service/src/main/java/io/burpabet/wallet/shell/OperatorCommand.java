package io.burpabet.wallet.shell;

import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;
import io.burpabet.common.shell.JurisdictionValueProvider;
import io.burpabet.common.util.Money;
import io.burpabet.common.util.RandomData;
import io.burpabet.wallet.model.AccountType;
import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;
import io.burpabet.wallet.service.BatchService;
import io.burpabet.wallet.service.NoSuchAccountException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class OperatorCommand extends AbstractShellComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AtomicInteger counter = new AtomicInteger();

    @Autowired
    private BatchService batchService;

    @Autowired
    private AnsiConsole ansiConsole;

    @Value("${server.port}")
    private int port;

    @ShellMethod(value = "Reset all account data", key = {"reset"})
    public void reset() {
        batchService.deleteAllInBatch();
        ansiConsole.cyan("Done!").nl();
    }

    @ShellMethod(value = "Create a set of operator accounts (optional)", key = {"co", "create-operators"})
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
                .forEach(j -> batchService.createOperatorAccounts(count,
                        () -> OperatorAccount.builder()
                                .withJurisdiction(j.name())
                                .withBalance(Money.of(balance, currency))
                                .withName("operator-" + j.name() + "-" + counter.incrementAndGet()) // not unique
                                .withDescription(RandomData.randomRoachFact())
                                .withAccountType(AccountType.LIABILITY)
                                .withAllowNegative(true) // allowed for operators
                                .build(), operatorAccount -> {
                            logger.info("Created operator account '%s' in %s"
                                    .formatted(operatorAccount.getName(), operatorAccount.getJurisdiction()));
                        }));
    }

    @ShellMethod(value = "Create a set of customer accounts (optional)", key = {"cc", "create-customers"})
    public void createCustomers(
            @ShellOption(help = "initial account balance (can go negative)", defaultValue = "0.00") String balance,
            @ShellOption(help = "account currency", defaultValue = "USD") String currency,
            @ShellOption(help = "number of accounts per jurisdiction", defaultValue = "10") int count,
            @ShellOption(help = "operator jurisdiction (if picked by random)", defaultValue = "SE",
                    valueProvider = JurisdictionValueProvider.class) String jurisdiction,
            @ShellOption(help = "operator id (random if omitted)", defaultValue = ShellOption.NULL,
                    valueProvider = OperatorValueProvider.class) String operator
    ) {
        OperatorAccount operatorAccount;

        if (Objects.nonNull(operator)) {
            UUID id = UUID.fromString(operator);
            operatorAccount = batchService.findOperatorAccountById(id)
                    .orElseThrow(() -> new NoSuchAccountException(id));
        } else {
            operatorAccount = batchService.findOperatorAccounts(jurisdiction)
                    .stream()
                    .findAny()
                    .orElseThrow(() -> new OperatorsNotFoundException(
                            "No operators found for jurisdiction: " + jurisdiction));
        }

        batchService.createCustomerAccounts(count,
                () -> CustomerAccount.builder()
                        .withJurisdiction(operatorAccount.getJurisdiction())
                        .withOperatorId(operatorAccount.getId())
                        .withBalance(Money.of(balance, currency))
                        .withName(RandomData.randomFullName())
                        .withDescription(RandomData.randomRoachFact())
                        .withAccountType(AccountType.ASSET)
                        .withAllowNegative(false)
                        .build(), customerAccount -> {
                    logger.info("Created customer account '%s' in %s"
                            .formatted(customerAccount.getName(), customerAccount.getJurisdiction()));
                });
    }

    @ShellMethod(value = "Grant extra bonus to operator customers", key = {"g", "grant"})
    public void grant(
            @ShellOption(help = "bonus amount", defaultValue = "50.00") String amount,
            @ShellOption(help = "bonus currency", defaultValue = "USD") String currency,
            @ShellOption(help = "operator id (all in jurisdiction if omitted)",
                    defaultValue = ShellOption.NULL,
                    valueProvider = OperatorValueProvider.class) String operator,
            @ShellOption(help = "operator jurisdiction (if picked by random)",
                    defaultValue = "SE",
                    valueProvider = JurisdictionValueProvider.class) String jurisdiction
    ) {
        List<OperatorAccount> operatorAccounts;

        if (Objects.nonNull(operator)) {
            UUID id = UUID.fromString(operator);
            operatorAccounts = List.of(batchService.findOperatorAccountById(id)
                    .orElseThrow(() -> new NoSuchAccountException(id)));
        } else {
            operatorAccounts = batchService.findOperatorAccounts(jurisdiction);
        }

        operatorAccounts.forEach(operatorAccount -> {
            Money total = batchService.grantBonus(operatorAccount, Money.of(amount, currency));
            ansiConsole.cyan("Granted %s in total for %s".formatted(total, operatorAccount.getName())).nl();
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
