package io.burpabet.wallet.shell;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.TableModel;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.burpabet.common.annotations.TimeTravel;
import io.burpabet.common.annotations.TimeTravelMode;
import io.burpabet.common.annotations.TransactionBoundary;
import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;
import io.burpabet.common.shell.JurisdictionValueProvider;
import io.burpabet.common.util.Money;
import io.burpabet.common.util.Networking;
import io.burpabet.common.util.RandomData;
import io.burpabet.common.util.TableUtils;
import io.burpabet.wallet.model.Account;
import io.burpabet.wallet.model.AccountType;
import io.burpabet.wallet.model.CustomerAccount;
import io.burpabet.wallet.model.OperatorAccount;
import io.burpabet.wallet.repository.AccountRepository;
import io.burpabet.wallet.service.BatchService;
import io.burpabet.wallet.service.NoSuchAccountException;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class OperatorCommand extends AbstractShellComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final AtomicInteger counter = new AtomicInteger();

    @Autowired
    private AccountRepository accountRepository;

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
                .forEach(jur -> batchService.createOperatorAccounts(count,
                        () -> OperatorAccount.builder()
                                .withJurisdiction(jur)
                                .withBalance(Money.of(balance, currency))
                                .withName("operator-" + jur.name() + "-" + counter.incrementAndGet()) // not unique
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
                    valueProvider = JurisdictionValueProvider.class) Jurisdiction jurisdiction,
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
                    valueProvider = JurisdictionValueProvider.class) Jurisdiction jurisdiction
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

    @ShellMethod(value = "Print account balances", key = {"b", "balance"})
    @TransactionBoundary(timeTravel = @TimeTravel(mode = TimeTravelMode.FOLLOWER_READ))
    public void printBalance() {
        Page<Account> page = accountRepository.findAll(PageRequest.ofSize(64)
                .withSort(Sort.by("balance", "accountType").descending()));
        AtomicInteger n = new AtomicInteger();
        for (; ; ) {
            printPage(page, n.getAndIncrement());
            if (page.hasNext()) {
                page = accountRepository.findAll(page.nextPageable());
            } else {
                break;
            }
        }
    }

    private void printPage(Page<Account> page, int n) {
        ansiConsole.cyan(TableUtils.prettyPrint(
                new TableModel() {
                    @Override
                    public int getRowCount() {
                        return page.getNumberOfElements();
                    }

                    @Override
                    public int getColumnCount() {
                        return 5;
                    }

                    @Override
                    public Object getValue(int row, int column) {
                        if (row == 0) {
                            switch (column) {
                                case 0 -> {
                                    return "#";
                                }
                                case 1 -> {
                                    return "Name";
                                }
                                case 2 -> {
                                    return "Balance";
                                }
                                case 3 -> {
                                    return "Jurisdiction";
                                }
                                case 4 -> {
                                    return "ID";
                                }
                            }
                            return "??";
                        }

                        Account account = page.getContent().get(row - 1);
                        switch (column) {
                            case 0 -> {
                                return page.getNumber()+n;
                            }
                            case 1 -> {
                                return account.getName();
                            }
                            case 2 -> {
                                return account.getBalance();
                            }
                            case 3 -> {
                                return account.getJurisdiction();
                            }
                            case 4 -> {
                                return account.getId();
                            }
                        }
                        return "??";
                    }
                }));
    }

    @ShellMethod(value = "Print and API index url", key = {"u", "url"})
    public void url() throws IOException {
        ansiConsole.cyan("Public URL: %s"
                .formatted(ServletUriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(Networking.getPublicIP())
                        .port(port)
                        .build()
                        .toUriString())).nl();
        ansiConsole.cyan("Local URL: %s"
                .formatted(ServletUriComponentsBuilder.newInstance()
                        .scheme("http")
                        .host(Networking.getLocalIP())
                        .port(port)
                        .build()
                        .toUriString())).nl();
    }

    @ShellMethod(value = "Print random fact", key = {"f", "fact"})
    public void fact() {
        ansiConsole.cyan(RandomData.randomRoachFact()).nl();
    }

}
