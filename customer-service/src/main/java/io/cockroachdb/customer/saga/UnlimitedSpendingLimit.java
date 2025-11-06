package io.cockroachdb.customer.saga;

import io.cockroachdb.betting.common.util.Money;
import io.cockroachdb.customer.service.SpendingLimit;

public class UnlimitedSpendingLimit implements SpendingLimit {
    @Override
    public boolean acquirePermission(Money amount) {
        return true;
    }

    @Override
    public void releasePermission(Money amount) {

    }

    @Override
    public String description() {
        return "unlimited";
    }
}
