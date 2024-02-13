package io.burpabet.customer.service;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.stream.IntStream;

import io.burpabet.common.util.Money;

public class SpendingLimitTest {
    @Test
    public void whenSpendingOverBudget_expectLimiting() {
        SpendingLimit spendingLimit = new SimpleSpendingLimit(
                Money.of("100.00", Money.USD), Duration.ofSeconds(5));

        IntStream.rangeClosed(1, 100).forEach(value -> {
            System.out.println(spendingLimit);
            Assertions.assertTrue(spendingLimit.acquirePermission(Money.of("1.00", Money.USD)));
        });

        Assertions.assertFalse(spendingLimit.acquirePermission(Money.of("1.00", Money.USD)));

        try {
            Thread.sleep(5 * 1_000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        IntStream.rangeClosed(1, 100).forEach(value -> {
            System.out.println(spendingLimit);
            Assertions.assertTrue(spendingLimit.acquirePermission(Money.of("1.00", Money.USD)));
        });

        Assertions.assertFalse(spendingLimit.acquirePermission(Money.of("1.00", Money.USD)));
    }
}
