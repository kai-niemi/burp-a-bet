package io.burpabet.customer.service;

import io.burpabet.common.util.Money;

public interface SpendingLimit {
    boolean acquirePermission(Money amount);

    void releasePermission(Money amount);
}
