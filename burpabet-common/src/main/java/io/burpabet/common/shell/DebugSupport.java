package io.burpabet.common.shell;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.springframework.data.util.Pair;

import io.burpabet.common.domain.AbstractJourney;
import io.burpabet.common.domain.Status;
import io.burpabet.common.util.AsciiArt;

public abstract class DebugSupport {
    private static final String NL = "\n";

    private DebugSupport() {
    }

    public static void logJourneyCompletion(Logger logger,
                                            String title,
                                            Pair<String, AbstractJourney> left,
                                            Pair<String, AbstractJourney> right,
                                            Status status) {
        StringBuilder sb = new StringBuilder()
                .append(NL);

        Map<String, Object> leftTuples = new LinkedHashMap<>();
        left.getSecond().debugTuples(leftTuples);

        sb.append(left.getFirst())
                .append(":")
                .append(NL);
        leftTuples.forEach((k, v) -> sb.append("%20s: %s".formatted(k, Objects.toString(v))).append(NL));

        Map<String, Object> rightTuples = new LinkedHashMap<>();
        right.getSecond().debugTuples(rightTuples);

        sb.append(right.getFirst())
                .append(":")
                .append(NL);
        rightTuples.forEach((k, v) -> sb.append("%20s: %s".formatted(k, Objects.toString(v))).append(NL));

        switch (status) {
            case PENDING -> logger.info(sb.append("%s - %s - %s".formatted(title, status, AsciiArt.shrug()))
                    .toString());
            case APPROVED -> logger.info(sb.append("%s - %s - %s".formatted(title, status, AsciiArt.happy()))
                    .toString());
            case REJECTED -> logger.warn(sb.append("%s - %s - %s".formatted(title, status, AsciiArt.flipTableGently()))
                    .toString());
            case ROLLBACK -> logger.warn(sb.append("%s - %s - %s".formatted(title, status, AsciiArt.flipTableRoughly()))
                    .toString());
        }
    }
}
