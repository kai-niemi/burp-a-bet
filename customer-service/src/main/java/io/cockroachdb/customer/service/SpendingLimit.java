package io.cockroachdb.customer.service;

import io.cockroachdb.betting.common.util.Money;

public interface SpendingLimit {
    boolean acquirePermission(Money amount);

    void releasePermission(Money amount);

    String description();
}
