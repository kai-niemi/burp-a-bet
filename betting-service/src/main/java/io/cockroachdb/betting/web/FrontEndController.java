package io.cockroachdb.betting.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.client.RestClientException;
import org.springframework.web.servlet.view.RedirectView;

import io.cockroachdb.betting.model.Race;
import io.cockroachdb.betting.service.BetPlacementService;
import io.cockroachdb.betting.service.BetSettlementService;
import io.cockroachdb.betting.service.RaceService;
import io.cockroachdb.betting.shell.HypermediaClient;
import io.cockroachdb.betting.common.domain.BetPlacement;
import io.cockroachdb.betting.common.domain.Outcome;
import io.cockroachdb.betting.common.util.Money;

import static io.cockroachdb.betting.shell.HypermediaClient.PAGED_CUSTOMER_MODEL_TYPE;
import static io.cockroachdb.betting.shell.HypermediaClient.PAGED_MODEL_TYPE;

@Controller
public class FrontEndController {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private RaceService raceService;

    @Autowired
    private BetPlacementService betPlacementService;

    @Autowired
    private BetSettlementService betSettlementService;

    @Autowired
    private HypermediaClient hypermediaClient;

    @GetMapping(path = "/customers")
    public Callable<String> listCustomers(
            @PageableDefault(size = 15) Pageable page, Model model) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page.getPageNumber());
        parameters.put("size", page.getPageSize());

        return () -> hypermediaClient.traverseCustomerApi(traverson -> {
            PagedModel<CustomerModel> customerPage = traverson
                    .follow("customer:all")
                    .withTemplateParameters(parameters)
                    .toObject(PAGED_CUSTOMER_MODEL_TYPE);

            PagedModel.PageMetadata pm = Objects.requireNonNull(customerPage).getMetadata();

            model.addAttribute("customerPage", customerPage);

            if (customerPage.hasLink(IanaLinkRelations.PREV)) {
                model.addAttribute("previousPageNumber", pm.getNumber() - 1);
            }
            if (customerPage.hasLink(IanaLinkRelations.NEXT)) {
                model.addAttribute("nextPageNumber", pm.getNumber() + 1);
            }

            int totalPages = (int) pm.getTotalPages();
            if (totalPages > 0) {
                List<Integer> pageNumbers = IntStream.range(0, totalPages)
                        .boxed()
                        .collect(Collectors.toList());
                model.addAttribute("pageNumbers", pageNumbers);
            }

            return "customers";
        });
    }

    @GetMapping(value = "/place-bets")
    public Callable<RedirectView> placeRandomBets() {
        try {
            PagedModel<Map<String, Object>> collection = hypermediaClient
                    .traverseCustomerApi(traverson -> Objects.requireNonNull(traverson
                            .follow("customer:all")
                            .withTemplateParameters(Map.of("page", 0, "size", 256))
                            .toObject(PAGED_MODEL_TYPE)));

            collection.getContent().forEach(map -> {
                BetPlacement betPlacement = new BetPlacement();
                betPlacement.setEventId(UUID.randomUUID());
                betPlacement.setCustomerId(UUID.fromString(map.get("id").toString()));
                betPlacement.setStake(Money.of("5.00", Money.USD));
                betPlacement.setRaceId(raceService.getRandomRace().getId());

                betPlacementService.placeBet(betPlacement);
            });
        } catch (RestClientException e) {
            logger.warn("Customer API error: " + e.getMessage());
        }

        return () -> new RedirectView("/bets-placed");
    }

    @GetMapping(value = "/settle-bets/{outcome}")
    public Callable<RedirectView> settleAllBets(@PathVariable("outcome") Outcome outcome) {
        Page<Race> page = raceService.findRacesWithUnsettledBets(PageRequest.ofSize(100));
        for (; ; ) {
            page.forEach(x -> betSettlementService.settleBets(x.getId(), outcome));
            if (page.hasNext()) {
                page = raceService.findRacesWithUnsettledBets(page.nextPageable());
            } else {
                break;
            }
        }
        return () -> new RedirectView("/bets-placed");
    }
}
