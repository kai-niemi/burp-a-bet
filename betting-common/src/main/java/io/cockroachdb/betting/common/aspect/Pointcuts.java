package io.cockroachdb.betting.common.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import io.cockroachdb.betting.common.annotations.OutboxOperation;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;

@Aspect
public class Pointcuts {
    /**
     * Pointcut expression matching all transactional boundaries.
     */
    @Pointcut("execution(public * *(..)) "
              + "&& @annotation(transactionBoundary)")
    public void anyTransactionBoundaryOperation(TransactionBoundary transactionBoundary) {
    }

    /**
     * Pointcut expression matching all outbox event operations.
     */
    @Pointcut("execution(public * *(..)) "
              + "&& @annotation(outboxPayload)")
    public void anyOutboxEventOperation(OutboxOperation outboxPayload) {
    }
}
