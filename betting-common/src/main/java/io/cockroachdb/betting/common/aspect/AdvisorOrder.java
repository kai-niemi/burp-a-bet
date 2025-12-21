package io.cockroachdb.betting.common.aspect;

import org.springframework.core.Ordered;

/**
 * Ordering constants for transaction advisors, ordered by highest
 * priority from top down.
 */
public interface AdvisorOrder {
    /**
     * Retry advice should have top priority, before any transaction is created.
     */
    int TRANSACTION_BEFORE_ADVISOR = Ordered.LOWEST_PRECEDENCE - 5;

    /**
     * Transaction manager advice must come after any retry advisor.
     */
    int TRANSACTION_BOUNDARY_ADVISOR = Ordered.LOWEST_PRECEDENCE - 4;

    /**
     * Transaction session attribute advice only make sense within a transaction scope.
     */
    int TRANSACTION_CONTEXT_ADVISOR = Ordered.LOWEST_PRECEDENCE - 3;

    /**
     * Any post business transaction advice, potentially within a transaction scope.
     */
    int TRANSACTION_AFTER_ADVISOR = Ordered.LOWEST_PRECEDENCE - 2;
}
