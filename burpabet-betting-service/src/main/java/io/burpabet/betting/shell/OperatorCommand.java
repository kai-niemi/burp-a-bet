package io.burpabet.betting.shell;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedModel;
import org.springframework.shell.standard.AbstractShellComponent;
import org.springframework.shell.standard.EnumValueProvider;
import org.springframework.shell.standard.ShellCommandGroup;
import org.springframework.shell.standard.ShellComponent;
import org.springframework.shell.standard.ShellMethod;
import org.springframework.shell.standard.ShellOption;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import io.burpabet.betting.model.Race;
import io.burpabet.betting.service.BetPlacementService;
import io.burpabet.betting.service.BetSettlementService;
import io.burpabet.betting.service.RaceService;
import io.burpabet.betting.shell.support.WorkloadExecutor;
import io.burpabet.common.domain.BetPlacement;
import io.burpabet.common.domain.Jurisdiction;
import io.burpabet.common.domain.Outcome;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;
import io.burpabet.common.shell.JurisdictionValueProvider;
import io.burpabet.common.util.Money;
import io.burpabet.common.util.Networking;
import io.burpabet.common.util.RandomData;

import static io.burpabet.betting.shell.HypermediaClient.PAGED_MODEL_TYPE;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class OperatorCommand extends AbstractShellComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RaceService raceService;

    @Autowired
    private BetPlacementService betPlacementService;

    @Autowired
    private BetSettlementService betSettlementService;

    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private AnsiConsole ansiConsole;

    @Autowired
    private WorkloadExecutor workloadExecutor;

    @Value("${server.port}")
    private int port;

    @ShellMethod(value = "Reset all betting data", key = {"reset"})
    public void reset() {
        betPlacementService.deleteAllInBatch();
        betSettlementService.deleteAllInBatch();
        ansiConsole.cyan("Done!").nl();
    }

    @ShellMethod(value = "Place a bet", key = {"pb", "place-bet", "burp"})
    public void placeBet(
            @ShellOption(help = "customer id (empty denotes all in jurisdiction)",
                    valueProvider = CustomerValueProvider.class,
                    value = {"customer"},
                    defaultValue = ShellOption.NULL) String customerId,
            @ShellOption(help = "customer jurisdiction (empty denotes all)",
                    valueProvider = JurisdictionValueProvider.class,
                    value = {"jurisdiction"},
                    defaultValue = "SE") Jurisdiction jurisdiction,
            @ShellOption(help = "race id by track or horse (empty denotes random)",
                    valueProvider = RaceValueProvider.class,
                    value = {"race"},
                    defaultValue = ShellOption.NULL) String raceId,
            @ShellOption(help = "bet stake to wager (in USD)",
                    defaultValue = "5.00",
                    valueProvider = StakeValueProvider.class) String stake,
            @ShellOption(help = "number of bets per customer",
                    defaultValue = "1") int count,
            @ShellOption(help = "duration for placements in seconds (>0 overrides count)",
                    defaultValue = "0") int duration
    ) {
        final Collection<Map<String, Object>> customerMap = new ArrayList<>();

        if (customerId == null) {
            try {
                PagedModel<Map<String, Object>> collection = hypermediaClient.traverseCustomerApi(
                        traverson -> Objects.requireNonNull(traverson
                                .follow("customer:all")
                                .withTemplateParameters(Map.of("jurisdiction", jurisdiction))
                                .toObject(PAGED_MODEL_TYPE)));
                customerMap.addAll(collection.getContent());
            } catch (RestClientException e) {
                logger.warn("Customer API error: " + e.getMessage());
            }
        } else {
            customerMap.add(Map.of("id", customerId, "name", "<unknown>"));
        }

        if (customerMap.isEmpty()) {
            logger.warn("No customer ID specified and no customers found");
            return;
        }

        customerMap.forEach(map -> {
            Callable<BetPlacement> c = () -> {
                BetPlacement betPlacement = new BetPlacement();
                betPlacement.setEventId(UUID.randomUUID());
                betPlacement.setCustomerId(UUID.fromString(map.get("id").toString()));
                betPlacement.setStake(Money.of(stake, "USD"));

                if (raceId != null) {
                    betPlacement.setRaceId(UUID.fromString(raceId));
                } else {
                    betPlacement.setRaceId(raceService.getRandomRace().getId());
                }

                return betPlacementService.placeBet(betPlacement);
            };

            if (duration > 0) {
                final Duration theDuration = Duration.ofSeconds(duration);

                logger.info("Placing bets for %s"
                        .formatted(theDuration));

                workloadExecutor.submit("Placement - " + map.get("name"), c,
                        x -> Instant.now().isBefore(Instant.now().plus(theDuration)));
            } else {
                IntStream.rangeClosed(1, count).forEach(value -> {
                    try {
                        BetPlacement bp = c.call();
                        logger.info("Bet placement journey started: %s".formatted(bp));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }

    @ShellMethod(value = "Settle bets for a given race (or all)",
            key = {"sb", "settle-bets"})
    public void settle(
            @ShellOption(help = "race id or all if omitted",
                    valueProvider = RaceValueProvider.class,
                    value = {"race"}, defaultValue = ShellOption.NULL) String race,
            @ShellOption(help = "outcome for bets or random if omitted",
                    value = {"outcome"}, defaultValue = ShellOption.NULL,
                    valueProvider = EnumValueProvider.class) Outcome outcome,
            @ShellOption(help = "query page size when iterating all races", defaultValue = "64") int pageSize,
            @ShellOption(help = "number of settlements", defaultValue = "1") int count,
            @ShellOption(help = "duration for settlements in seconds (>0 overrides count)", defaultValue = "0")
            int duration
    ) {
        final Duration theDuration = Duration.ofSeconds(duration);

        final AtomicInteger counter = new AtomicInteger();

        Callable<Void> c = () -> {
            Outcome o = outcome == null
                    ? ThreadLocalRandom.current().nextBoolean() ? Outcome.win : Outcome.lose
                    : outcome;
            if (race != null) {
                settleBets(counter.incrementAndGet(), UUID.fromString(race), o);
            } else {
                Page<Race> page = raceService.findRacesWithUnsettledBets(PageRequest.ofSize(pageSize));
                for (; ; ) {
                    page.forEach(x -> settleBets(counter.incrementAndGet(), x.getId(), o));
                    if (page.hasNext()) {
                        page = raceService.findRacesWithUnsettledBets(page.nextPageable());
                    } else {
                        break;
                    }
                }
            }
            return null;
        };

        if (duration > 0) {
            logger.info("Settling bets for %s".formatted(theDuration));

            workloadExecutor.submit("Settlement", c,
                    x -> Instant.now().isBefore(Instant.now().plus(theDuration)));
        } else {
            IntStream.rangeClosed(1, count).forEach(value -> {
                try {
                    c.call();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            });
        }
    }

    private void settleBets(int count, UUID raceId, Outcome outcome) {
        logger.info("Bet settlement journey %d started with outcome %s: %s"
                .formatted(count, outcome, raceId.toString()));
        betSettlementService.settleBets(raceId, outcome);
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
