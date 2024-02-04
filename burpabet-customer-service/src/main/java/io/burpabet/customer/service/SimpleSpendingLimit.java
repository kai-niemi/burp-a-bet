package io.burpabet.customer.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import io.burpabet.common.util.Money;

public class SimpleSpendingLimit implements SpendingLimit {
    private final Lock lock = new ReentrantLock();

    private final Money limitForPeriod;

    private final Duration limitRefreshPeriod;

    private Money budgetForPeriod;

    private LocalDateTime periodStart;

    public SimpleSpendingLimit(Money limitForPeriod, Duration limitRefreshPeriod) {
        this.limitForPeriod = limitForPeriod;
        this.limitRefreshPeriod = limitRefreshPeriod;
        this.budgetForPeriod = limitForPeriod;
        this.periodStart = LocalDateTime.now();
    }

    public LocalDateTime nextPeriodStart() {
        return periodStart.plus(limitRefreshPeriod);
    }

    @Override
    public boolean acquirePermission(Money amount) {
        try {
            lock.lock();
            if (LocalDateTime.now().isAfter(nextPeriodStart())) {
                budgetForPeriod = limitForPeriod;
                periodStart = LocalDateTime.now();
            }
            if (budgetForPeriod.minus(amount).isPositive()) {
                budgetForPeriod = budgetForPeriod.minus(amount);
                return true;
            }
            return false;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void releasePermission(Money amount) {
        try {
            lock.lock();
            budgetForPeriod = limitForPeriod;
            periodStart = LocalDateTime.now();
            budgetForPeriod = budgetForPeriod.plus(amount);
        } finally {
            lock.unlock();
        }
    }

    public Duration getLimitRefreshPeriod() {
        return limitRefreshPeriod;
    }

    public Money getLimitForPeriod() {
        return limitForPeriod;
    }

    public Money getBudgetForPeriod() {
        return budgetForPeriod;
    }

    @Override
    public String toString() {
        return "SpendingLimit{" +
                "limitForPeriod=" + limitForPeriod +
                ", limitRefreshPeriod=" + limitRefreshPeriod +
                ", budgetForPeriod=" + budgetForPeriod +
                ", periodStart=" + periodStart +
                '}';
    }
}
