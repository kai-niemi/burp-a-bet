package io.burpabet.customer.shell;

import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.domain.Registration;
import io.burpabet.common.domain.Status;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;
import io.burpabet.common.shell.JurisdictionValueProvider;
import io.burpabet.common.shell.ThrottledPredicates;
import io.burpabet.common.util.Networking;
import io.burpabet.common.util.RandomData;
import io.burpabet.customer.model.Customer;
import io.burpabet.customer.service.CustomerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.IntStream;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class OperatorCommand extends AbstractShellComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private CustomerService customerService;

    @Autowired
    private AnsiConsole ansiConsole;

    @Value("${server.port}")
    private int port;

    @ShellMethod(value = "Reset all customer data", key = {"reset"})
    public void reset() {
        customerService.deleteAllInBatch();
        ansiConsole.cyan("Done!").nl();
    }

    @ShellMethod(value = "Register a new customer", key = {"r", "register"})
    public void register(
            @ShellOption(help = "operator ID or random assigned if omitted",
                    valueProvider = OperatorAccountValueProvider.class,
                    value = {"operator"},
                    defaultValue = ShellOption.NULL) String operatorId,
            @ShellOption(help = "customer jurisdiction",
                    defaultValue = "SE",
                    valueProvider = JurisdictionValueProvider.class) Jurisdiction jurisdiction,
            @ShellOption(help = "number of registrations", defaultValue = "1") int count,
            @ShellOption(help = "registrations per minute", defaultValue = "120") int ratePerMin,
            @ShellOption(help = "registrations per sec", defaultValue = "5") int ratePerSec
    ) {
        if (count > 1) {
            logger.info("Creating %d registrations with rate limit of %d registrations per minute at max %d per sec"
                    .formatted(count, ratePerMin, ratePerSec));
        }

        IntStream.rangeClosed(1, count)
                .asLongStream()
                .unordered()
                .parallel()
                .takeWhile(ThrottledPredicates.longPredicate(ratePerMin, ratePerSec))
                .forEach(value -> {
                    Pair<String, String> pair = RandomData.randomFullNameAndEmail("burpabet.io");

                    Registration registration = customerService.registerCustomer(Customer.builder()
                            .withName(pair.getFirst())
                            .withEmail(pair.getSecond())
                            .withJurisdiction(jurisdiction)
                            .withStatus(Status.PENDING)
                            .withOperatorId(Objects.nonNull(operatorId) ? UUID.fromString(operatorId) : null)
                            .build());

                    logger.info("Registration journey %d/%d started: %s"
                            .formatted(value, count, registration.toString()));
                });
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
