package io.burpabet.customer.shell;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.CollectionModel;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import java.util.*;

import static io.burpabet.customer.shell.HypermediaClient.MAP_MODEL_TYPE;

public class OperatorAccountValueProvider implements ValueProvider {
    @Autowired
    private HypermediaClient hypermediaClient;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String p = completionContext.currentWordUpToCursor();
        if (p == null) {
            p = "";
        }
        String prefix = p;

        List<CompletionProposal> result = new ArrayList<>();

        hypermediaClient.traverseWalletApi(traverson -> {
            Map<String, Object> parameters = new HashMap<>();
            parameters.put("size", 50);
            parameters.put("shuffle", true);

            CollectionModel<Map<String, Object>> allOperators = traverson
                    .follow("wallet:all-operators")
                    .withTemplateParameters(parameters)
                    .toObject(MAP_MODEL_TYPE);

            Objects.requireNonNull(allOperators).getContent()
                    .forEach(m -> {
                        String name = (String) m.get("name");
                        if (name.startsWith(prefix)) {
                            result.add(new CompletionProposal((String) m.get("id"))
                                    .displayText((String) m.get("name")));
                        }
                    });

            return null;
        });

        return result;
    }
}


