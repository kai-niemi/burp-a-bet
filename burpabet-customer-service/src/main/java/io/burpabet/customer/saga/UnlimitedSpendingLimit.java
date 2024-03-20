package io.burpabet.customer.saga;

import io.burpabet.common.util.Money;
import io.burpabet.customer.service.SpendingLimit;

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
