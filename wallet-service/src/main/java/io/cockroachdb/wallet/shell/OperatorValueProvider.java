package io.cockroachdb.wallet.shell;

import io.cockroachdb.wallet.model.OperatorAccount;
import io.cockroachdb.wallet.repository.OperatorAccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class OperatorValueProvider implements ValueProvider {
    @Autowired
    private OperatorAccountRepository accountRepository;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String word = completionContext.currentWordUpToCursor();
        if (word == null) {
            word = "";
        }
        String prefix = word;

        List<CompletionProposal> result = new ArrayList<>();

        for (OperatorAccount account : accountRepository.findAll(PageRequest.ofSize(2 ^ 16))) {
            if (account.getName().startsWith(prefix)) {
                result.add(new CompletionProposal(
                        Objects.requireNonNull(account.getId()).toString())
                        .displayText(account.getName())
                        .description(account.getDescription())
                );
            }
        }

        return result;
    }
}
