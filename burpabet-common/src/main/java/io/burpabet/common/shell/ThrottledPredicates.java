package io.burpabet.common.shell;

import io.github.bucket4j.BlockingStrategy;
import io.github.bucket4j.Bucket;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;
import java.util.function.Predicate;

import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofSeconds;

public abstract class ThrottledPredicates {
    private ThrottledPredicates() {
    }

    public static LongPredicate longPredicate(int ratePerMin, int ratePerSec) {
        Bucket bucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(ratePerMin).refillGreedy(ratePerMin, ofMinutes(1)))
                .addLimit(limit -> limit.capacity(ratePerSec).refillGreedy(ratePerSec, ofSeconds(1)))
                .build();
        return (value) -> {
            try {
                return bucket.asBlocking().tryConsume(1,
                        TimeUnit.MINUTES.toNanos(1),
                        BlockingStrategy.PARKING);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        };
    }

    public static Predicate<Integer> countPredicate(int upperBound, int ratePerMin, int ratePerSec) {
        Bucket bucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(ratePerMin).refillGreedy(ratePerMin, ofMinutes(1)))
                .addLimit(limit -> limit.capacity(ratePerSec).refillGreedy(ratePerSec, ofSeconds(1)))
                .build();
        return (value) -> {
            try {
                bucket.asBlocking().tryConsume(1,
                        TimeUnit.MINUTES.toNanos(1),
                        BlockingStrategy.PARKING);
                return value < upperBound;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        };
    }

    public static Predicate<Integer> timePredicate(Instant futureTime, int ratePerMin, int ratePerSec) {
        Bucket bucket = Bucket.builder()
                .addLimit(limit -> limit.capacity(ratePerMin).refillGreedy(ratePerMin, ofMinutes(1)))
                .addLimit(limit -> limit.capacity(ratePerSec).refillGreedy(ratePerSec, ofSeconds(1)))
                .build();
        return (value) -> {
            try {
                bucket.asBlocking().tryConsume(1,
                        TimeUnit.MINUTES.toNanos(1),
                        BlockingStrategy.PARKING);
                return Instant.now().isBefore(futureTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        };
    }
}
