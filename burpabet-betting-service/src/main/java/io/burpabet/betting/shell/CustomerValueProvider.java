package io.burpabet.betting.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.PagedModel;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;
import org.springframework.web.client.RestClientException;

import static io.burpabet.betting.shell.HypermediaClient.MAP_MODEL_TYPE;
import static io.burpabet.betting.shell.HypermediaClient.PAGED_MODEL_TYPE;

public class CustomerValueProvider implements ValueProvider {
    @Autowired
    private HypermediaClient hypermediaClient;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String word = completionContext.currentWordUpToCursor();
        if (word == null) {
            word = "";
        }
        String prefix = word;

        List<CompletionProposal> result = new ArrayList<>();

        try {
            hypermediaClient.traverseCustomerApi(traverson -> {
                PagedModel<Map<String, Object>> allCustomers = traverson
                        .follow("customer:all")
                        .toObject(PAGED_MODEL_TYPE);

                Objects.requireNonNull(allCustomers).getContent()
                        .forEach(map -> {
                            String name = (String) map.get("name");
                            if (name.startsWith(prefix)) {
                                result.add(new CompletionProposal((String) map.get("id"))
                                        .displayText((String) map.get("name"))
                                );
                            }
                        });
                return null;
            });
        } catch (RestClientException e) {
            System.out.println("API unavailable: " + e.getMessage());
        }

        return result;
    }
}


