package io.burpabet.betting.web;

import io.burpabet.betting.shell.HypermediaClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.hateoas.IanaLinkRelations;
import org.springframework.hateoas.PagedModel;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static io.burpabet.betting.shell.HypermediaClient.PAGED_CUSTOMER_MODEL_TYPE;

@Controller
@RequestMapping(path = "/customers")
public class CustomerController {
    @Autowired
    private HypermediaClient hypermediaClient;

    @GetMapping
    public String findAll(
            @PageableDefault(size = 15) Pageable page, Model model) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("page", page.getPageNumber());
        parameters.put("size", page.getPageSize());

        return hypermediaClient.traverseCustomerApi(traverson -> {
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
}
