package io.burpabet.common.shell;

import java.util.concurrent.TimeUnit;
import java.util.function.LongPredicate;

import io.github.bucket4j.BlockingStrategy;
import io.github.bucket4j.Bucket;

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
                if (ratePerMin <= 0 || ratePerSec <= 0) {
                    return true;
                }
                return bucket.asBlocking().tryConsume(1,
                        TimeUnit.MINUTES.toNanos(1),
                        BlockingStrategy.PARKING);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        };
    }
}
