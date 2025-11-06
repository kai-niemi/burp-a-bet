package io.cockroachdb.customer.shell;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.EnumSet;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.util.Pair;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.shell.table.TableModel;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import io.cockroachdb.betting.common.domain.Registration;
import io.cockroachdb.betting.common.domain.Status;
import io.cockroachdb.betting.common.shell.AnsiConsole;
import io.cockroachdb.betting.common.shell.CommandGroups;
import io.cockroachdb.betting.common.shell.JurisdictionValueProvider;
import io.cockroachdb.betting.common.util.RandomData;
import io.cockroachdb.betting.common.util.TableUtils;
import io.cockroachdb.customer.model.Customer;
import io.cockroachdb.customer.saga.CustomerBettingFacade;
import io.cockroachdb.customer.service.CustomerService;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class OperatorCommand extends AbstractShellComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerBettingFacade customerBettingFacade;

    @Autowired
    private AnsiConsole ansiConsole;

    @ShellMethod(value = "Register a new customer", key = {"r", "register"})
    public void register(
            @ShellOption(help = "operator ID or random assigned if omitted",
                    valueProvider = OperatorAccountValueProvider.class,
                    value = {"operator"},
                    defaultValue = ShellOption.NULL) String operatorId,
            @ShellOption(help = "customer jurisdiction",
                    defaultValue = ShellOption.NULL,
                    valueProvider = JurisdictionValueProvider.class) Jurisdiction jurisdiction,
            @ShellOption(help = "number of registrations",
                    defaultValue = "1") int count,
            @ShellOption(help = "spending budget per minute (rate limit)",
                    defaultValue = "50.00") String spendingBudget
    ) {
        final EnumSet<Jurisdiction> all = EnumSet.allOf(Jurisdiction.class);

        IntStream.rangeClosed(1, count)
                .asLongStream()
                .unordered()
                .parallel()
                .forEach(value -> {
                    Pair<String, String> pair = RandomData.randomFullNameAndEmail("burpabet.io");

                    Jurisdiction jur = jurisdiction == null
                            ? all.stream()
                            .skip(ThreadLocalRandom.current().nextInt(all.size()))
                            .findFirst().get()
                            : jurisdiction;

                    Registration registration = customerService.registerCustomer(Customer.builder()
                            .withName(pair.getFirst())
                            .withEmail(pair.getSecond()) // unique index and collisions are possible
                            .withJurisdiction(jur)
                            .withStatus(Status.PENDING)
                            .withOperatorId(Objects.nonNull(operatorId) ? UUID.fromString(operatorId) : null)
                            .withSpendingBudget(new BigDecimal(spendingBudget)
                                    .setScale(2, RoundingMode.UNNECESSARY))
                            .build());

                    logger.info("Registration journey %d/%d started: %s"
                            .formatted(value, count, registration.toString()));
                });

        if (count > 1) {
            logger.info("All registration journeys (%d) started".formatted(count));
        }
    }

    @ShellMethod(value = "List registered customers", key = {"lc", "list"})
    public void listCustomers() {
        Page<Customer> page = customerService.findAll(PageRequest.ofSize(64)
                .withSort(Sort.by("jurisdiction").ascending()));

        for (; ; ) {
            printPage(page);
            if (page.hasNext()) {
                page = customerService.findAll(page.nextPageable());
            } else {
                break;
            }
        }
    }

    @ShellMethod(value = "Toggle spending limit check", key = {"toggle-limits", "tl"})
    public void toggleSpendingLimit() {
        customerBettingFacade.toggleSpendingLimits();
    }

    private void printPage(Page<Customer> page) {
        ansiConsole.cyan(TableUtils.prettyPrint(
                new TableModel() {
                    @Override
                    public int getRowCount() {
                        return page.getNumberOfElements() + 1;
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
                                    return "Status";
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

                        Customer customer = page.getContent().get(row - 1);
                        switch (column) {
                            case 0 -> {
                                return row;
                            }
                            case 1 -> {
                                return customer.getName();
                            }
                            case 2 -> {
                                return customer.getStatus();
                            }
                            case 3 -> {
                                return customer.getJurisdiction();
                            }
                            case 4 -> {
                                return customer.getId();
                            }
                        }
                        return "??";
                    }
                }));
    }

}
