package io.burpabet.betting.shell.support;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Supplier;

public class CallMetrics {
    private static final String HEADER_PATTERN = "%-35s %9s %7s %8s %10s %10s %10s %10s %10s %9s %9s";

    private static final String ROW_PATTERN = "%-35s %,9d %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %,9d %,9d";

    private static final String FOOTER_PATTERN = "%-35s %,9d %7.0f %c%7.1f %10.1f %10.2f %10.2f %10.2f %10.2f %,9d %,9d";

    private static final SortedMap<String, CallContext> METRICS
            = Collections.synchronizedSortedMap(new TreeMap<>());

    public static CallContext of(String name, Supplier<Integer> concurrency) {
        return METRICS.computeIfAbsent(name,
                supplier -> new CallContext(name, concurrency));
    }

    public static void clear() {
        METRICS.clear();
    }

    public static void print(MetricsListener listener) {
        listener.header(
                HEADER_PATTERN.formatted(
                        "metric",
                        "threads",
                        "time(s)",
                        "op/s",
                        "op/m",
                        "p90",
                        "p99",
                        "p99.9",
                        "mean",
                        "ok",
                        "fail"
                ));
        listener.header(
                HEADER_PATTERN.formatted(
                        separator(35),// metric
                        separator(9), // threads
                        separator(7), // time
                        separator(8), // ops
                        separator(10), // opm
                        separator(10), // p90
                        separator(10), // p99
                        separator(10), // p99.9
                        separator(10), // mean
                        separator(9), // success
                        separator(9) // fail
                ));

        METRICS.forEach((key, value) -> listener.body(value.printStats(ROW_PATTERN)));

        int concurrencySum = METRICS.values().stream().mapToInt(CallContext::concurrency).sum();
        double timeAvg = METRICS.values().stream().mapToDouble(CallContext::executionTimeSeconds).average()
                .orElse(0);
        double opsPerSecSum = METRICS.values().stream().mapToDouble(CallContext::opsPerSec).sum();
        double opsPerMinSum = METRICS.values().stream().mapToDouble(CallContext::opsPerMin).sum();
        double p90 = METRICS.values().stream().mapToDouble(CallContext::p90).average().orElse(0);
        double p99 = METRICS.values().stream().mapToDouble(CallContext::p99).average().orElse(0);
        double p999 = METRICS.values().stream().mapToDouble(CallContext::p999).average().orElse(0);
        double meanTime = METRICS.values().stream().mapToDouble(CallContext::mean).average().orElse(0);
        int successSum = METRICS.values().stream().mapToInt(CallContext::successfulCalls).sum();
        int failSum = METRICS.values().stream().mapToInt(CallContext::failedCalls).sum();

        listener.footer(
                FOOTER_PATTERN.formatted(
                        "sum/avg",
                        concurrencySum,
                        timeAvg,
                        ' ',
                        opsPerSecSum,
                        opsPerMinSum,
                        p90,
                        p99,
                        p999,
                        meanTime,
                        successSum,
                        failSum
                ));
    }

    private static String separator(int len) {
        return new String(new char[len]).replace('\0', '-');
    }
}
