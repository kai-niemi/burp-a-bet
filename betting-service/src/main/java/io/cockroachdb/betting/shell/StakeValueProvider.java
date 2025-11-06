package io.cockroachdb.betting.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

public class StakeValueProvider implements ValueProvider {
    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        List<CompletionProposal> result = new ArrayList<>();
        IntStream.rangeClosed(1, 20).forEach(value -> {
            result.add(new CompletionProposal(value + ".00"));
        });
        return result;
    }
}
