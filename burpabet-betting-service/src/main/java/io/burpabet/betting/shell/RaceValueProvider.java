package io.burpabet.betting.shell;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.shell.CompletionContext;
import org.springframework.shell.CompletionProposal;
import org.springframework.shell.standard.ValueProvider;

import io.burpabet.betting.model.Race;
import io.burpabet.betting.repository.RaceRepository;

public class RaceValueProvider implements ValueProvider {
    @Autowired
    private RaceRepository raceRepository;

    @Override
    public List<CompletionProposal> complete(CompletionContext completionContext) {
        String word = completionContext.currentWordUpToCursor();
        if (word == null) {
            word = "";
        }
        String prefix = word;

        List<CompletionProposal> result = new ArrayList<>();

        for (Race race : raceRepository.findAllByPage(PageRequest.ofSize(
                prefix.isEmpty() ? 100 : 2^16))) {
            if (race.getTrack().startsWith(prefix)) {
                result.add(new CompletionProposal(
                        Objects.requireNonNull(race.getId()).toString())
                        .displayText(race.getTrack())
                        .description(race.getHorse() + " " + race.getOdds())
                        .complete(true)
                );
            }
        }

        return result;
    }
}
