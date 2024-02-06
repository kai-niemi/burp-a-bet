package io.burpabet.betting.shell;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.hateoas.PagedModel;
import org.springframework.shell.Availability;
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
import io.burpabet.betting.service.BettingService;
import io.burpabet.common.domain.BetPlacement;
import io.burpabet.common.domain.Outcome;
import io.burpabet.common.shell.AnsiConsole;
import io.burpabet.common.shell.CommandGroups;
import io.burpabet.common.shell.ThrottledPredicates;
import io.burpabet.common.util.Money;
import io.burpabet.common.util.Networking;
import io.burpabet.common.util.RandomData;

import static io.burpabet.betting.shell.HypermediaClient.PAGED_MODEL_TYPE;

@ShellComponent
@ShellCommandGroup(CommandGroups.OPERATOR)
public class OperatorCommand extends AbstractShellComponent {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private BettingService bettingService;

    @Autowired
    private BetPlacementService betPlacementService;

    @Autowired
    private BetSettlementService betSettlementService;

    @Autowired
    private HypermediaClient hypermediaClient;

    @Autowired
    private AnsiConsole ansiConsole;

    @Value("${server.port}")
    private int port;

    public Availability serviceAvailable() {
        return hypermediaClient.traverseCustomerApi(traverson -> {
            try {
                traverson.follow().toObject(String.class);
                return Availability.available();
            } catch (RestClientException e) {
                return Availability.unavailable("Customer API not available (" + e.getMessage() + ")");
            }
        });
    }

    @ShellMethod(value = "Reset all betting data", key = {"reset"})
    public void reset() {
        bettingService.deleteAllInBatch();
        ansiConsole.cyan("Done!").nl();
    }

    @ShellMethod(value = "Place a bet", key = {"pb", "place-bet", "burp"})
    public void placeBet(
            @ShellOption(help = "customer id (empty denotes random)",
                    valueProvider = CustomerValueProvider.class,
                    value = {"customer"}, defaultValue = ShellOption.NULL) String customerId,
            @ShellOption(help = "race id by track or horse (empty denotes random)",
                    valueProvider = RaceValueProvider.class,
                    value = {"race"}, defaultValue = ShellOption.NULL) String raceId,
            @ShellOption(help = "bet stake to wager (in USD)", defaultValue = "5.00",
                    valueProvider = StakeValueProvider.class) String stake,
            @ShellOption(help = "number of bets", defaultValue = "1") int count,
            @ShellOption(help = "bets per minute", defaultValue = "120") int ratePerMin,
            @ShellOption(help = "bets per sec", defaultValue = "5") int ratePerSec
    ) {
        if (count > 1) {
            logger.info("Placing %,d bets with rate limit of %d bets per minute at max %d per sec"
                    .formatted(count, ratePerMin, ratePerSec));
        }

        final Collection<Map<String, Object>> all = new ArrayList<>();

        if (customerId == null) {
            try {
                PagedModel<Map<String, Object>> collection = hypermediaClient.traverseCustomerApi(
                        traverson -> Objects.requireNonNull(traverson
                                .follow("customer:all")
                                .toObject(PAGED_MODEL_TYPE)));
                all.addAll(collection.getContent());
            } catch (RestClientException e) {
                logger.warn("Customer API error: " + e.getMessage());
            }
        }

        if (customerId == null && all.isEmpty()) {
            logger.warn("No customer ID specified and no customers found");
            return;
        }

        IntStream.rangeClosed(1, count)
                .asLongStream()
                .unordered()
                .parallel()
                .takeWhile(ThrottledPredicates.longPredicate(ratePerMin, ratePerSec))
                .forEach(value -> {
                    BetPlacement betPlacement = new BetPlacement();
                    if (!all.isEmpty()) {
                        Map<String, Object> tuples = RandomData.selectRandom(all);
                        betPlacement.setCustomerId(UUID.fromString(tuples.get("id").toString()));
                        betPlacement.setCustomerName(tuples.get("name").toString());
                    } else {
                        betPlacement.setCustomerId(UUID.fromString(customerId));
                    }

                    betPlacement.setStake(Money.of(stake, "USD"));

                    if (raceId != null) {
                        betPlacement.setRaceId(UUID.fromString(raceId));
                    } else {
                        betPlacement.setRaceId(bettingService.getRandomRace().getId());
                    }

                    betPlacement = betPlacementService.placeBet(betPlacement);

                    logger.info("Bet placement journey started: %s".formatted(betPlacement));
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
            @ShellOption(help = "settlements per minute", defaultValue = "30") int ratePerMin,
            @ShellOption(help = "settlements per sec", defaultValue = "2") int ratePerSec
    ) {
        if (count > 1) {
            logger.info("Settling %,d bets with rate limit of %d settlements per minute at max %d per sec"
                    .formatted(count, ratePerMin, ratePerSec));
        }

        IntStream.rangeClosed(1, count)
                .asLongStream()
                .unordered()
                .parallel()
                .takeWhile(ThrottledPredicates.longPredicate(ratePerMin, ratePerSec))
                .forEach(value -> {
                    if (race != null) {
                        settleBets(value, count, bettingService.getRaceById(UUID.fromString(race)), outcome);
                    } else {
                        Page<Race> page = bettingService.findRacesWithUnsettledBets(PageRequest.ofSize(pageSize));
                        for (; ; ) {
                            page.forEach(r -> settleBets(value, count, r, outcome));
                            if (page.hasNext()) {
                                page = bettingService.findRacesWithUnsettledBets(page.nextPageable());
                            } else {
                                break;
                            }
                        }
                    }
                });
    }

    private void settleBets(long value, int count, Race race, Outcome outcome) {
        Outcome o = outcome == null
                ? ThreadLocalRandom.current().nextBoolean() ? Outcome.win : Outcome.lose
                : outcome;

        logger.info("Bet settlement journey %d/%d started with outcome %s: %s"
                .formatted(value, count, o, race.toString()));

        betSettlementService.settleBets(race, o);
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
