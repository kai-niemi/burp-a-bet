package io.burpabet.common.aspect;

import org.springframework.core.Ordered;

/**
 * Ordering constants for transaction advisors, ordered by highest
 * priority from top down.
 */
public abstract class AdvisorOrder {
    private AdvisorOrder() {
    }

    public static final int TRANSACTION_RETRY_ADVISOR = Ordered.LOWEST_PRECEDENCE - 5;

    public static final int TRANSACTION_MANAGER_ADVISOR = Ordered.LOWEST_PRECEDENCE - 4;

    public static final int CHANGE_FEED_ADVISOR = Ordered.LOWEST_PRECEDENCE - 3;

    public static final int TRANSACTION_ATTRIBUTES_ADVISOR = Ordered.LOWEST_PRECEDENCE - 2;
}
