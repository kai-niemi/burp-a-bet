package io.burpabet.betting.shell.support;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CallContext {
    private final String name;

    private final Supplier<Integer> concurrency;

    private final long startTime = System.nanoTime();

    private final AtomicInteger successful = new AtomicInteger();

    private final AtomicInteger failed = new AtomicInteger();

    private final List<Snapshot> snapshots = Collections.synchronizedList(new LinkedList<>());

    protected CallContext(String name, Supplier<Integer> concurrency) {
        this.name = name;
        this.concurrency = concurrency;
    }

    protected long before() {
        return System.nanoTime();
    }

    protected void after(long beginTime, Throwable t) {
        evictTail();

        // Purge snapshots older than 1min
        snapshots.add(new Snapshot(beginTime));

        if (t != null) {
            failed.incrementAndGet();
        } else {
            successful.incrementAndGet();
        }
    }

    private void evictTail() {
        final Instant bound = Instant.now().minusSeconds(60);
        snapshots.removeIf(snapshot -> snapshot.getMark().isBefore(bound));
    }

    protected double executionTimeSeconds() {
        return Duration.ofNanos(System.nanoTime() - startTime).toMillis() / 1000.0;
    }

    protected String printStats(String pattern) {
        evictTail();

        List<Double> latencies = sortedLatencies();

        double p90 = 0;
        double p99 = 0;
        double p999 = 0;

        if (snapshots.size() > 1) {
            p90 = percentile(latencies, .9);
            p99 = percentile(latencies, .99);
            p999 = percentile(latencies, .999);
        }

        final double opsPerSec = opsPerSec();

        return pattern.formatted(
                name,
                concurrency.get(),
                executionTimeSeconds(),
                ' ',
                opsPerSec,
                opsPerSec * 60,
                p90,
                p99,
                p999,
                mean(),
                successful.get(),
                failed.get()
        );
    }

    protected double opsPerSec() {
        final int size = snapshots.size();
        return size / Math.max(1,
                Duration.ofNanos(
                                (System.nanoTime() - (snapshots.isEmpty() ? 0 : snapshots.get(0).endTime)))
                        .toMillis() / 1000.0);
    }

    protected double opsPerMin() {
        return opsPerSec() * 60;
    }

    protected double p90() {
        return percentile(sortedLatencies(), .9);
    }

    protected double p99() {
        return percentile(sortedLatencies(), .99);
    }

    protected double p999() {
        return percentile(sortedLatencies(), .999);
    }

    protected int successfulCalls() {
        return successful.get();
    }

    protected int failedCalls() {
        return failed.get();
    }

    protected int concurrency() {
        return concurrency.get();
    }

    protected double mean() {
        List<Snapshot> copy = new ArrayList<>(snapshots);
        return copy.stream().mapToDouble(Snapshot::durationMillis)
                .average()
                .orElse(0);
    }

    private List<Double> sortedLatencies() {
        List<Snapshot> copy = new ArrayList<>(snapshots);
        return copy.stream().map(Snapshot::durationMillis)
                .sorted()
                .collect(Collectors.toList());
    }

    private double percentile(List<Double> latencies, double percentile) {
        if (percentile < 0 || percentile > 1) {
            throw new IllegalArgumentException(">=0 N <=1");
        }
        if (!latencies.isEmpty()) {
            int index = (int) Math.ceil(percentile * latencies.size());
            return latencies.get(index - 1);
        }
        return 0;
    }

    protected static class Snapshot implements Comparable<Snapshot> {
        final Instant mark = Instant.now();

        final long endTime = System.nanoTime();

        final long beginTime;

        Snapshot(long beginTime) {
            if (beginTime > endTime) {
                throw new IllegalArgumentException();
            }
            this.beginTime = beginTime;
        }

        double durationMillis() {
            return (endTime - beginTime) / 1_000_000.0;
        }

        Instant getMark() {
            return mark;
        }

        @Override
        public int compareTo(Snapshot o) {
            return Long.compare(o.endTime, endTime);
        }
    }
}
