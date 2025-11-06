package io.cockroachdb.betting.common.shell;

import io.cockroachdb.betting.common.domain.Jurisdiction;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JurisdictionValueProvider implements ValueProvider {
    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String p = completionContext.currentWordUpToCursor();
        if (p == null) {
            p = "";
        }
        String prefix = p;

        List<CompletionProposal> result = new ArrayList<>();

        Map<Jurisdiction, List<String>> codes = new LinkedHashMap<>();

        EnumSet.allOf(Jurisdiction.class).forEach(jurisdiction -> codes.computeIfAbsent(jurisdiction,
                s -> new ArrayList<>()).add(jurisdiction.name()));

        codes.forEach((key, value) -> value.forEach(name -> {
            if (name.startsWith(prefix)) {
                result.add(new CompletionProposal(name)
                        .displayText(key.name())
                        .category(key.getRegion())
                        .description(key.getCountry()));
            }
        }));

        return result;
    }
}
