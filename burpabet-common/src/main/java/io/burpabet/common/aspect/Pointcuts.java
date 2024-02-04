package io.burpabet.common.aspect;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import io.burpabet.common.annotations.OutboxOperation;
import io.burpabet.common.annotations.Retryable;
import io.burpabet.common.annotations.TransactionBoundary;

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
     * Pointcut expression matching all retryable operations.
     */
    @Pointcut("execution(public * *(..)) "
            + "&& @annotation(retryable)")
    public void anyRetryableOperation(Retryable retryable) {
    }

    /**
     * Pointcut expression matching all outbox event operations.
     */
    @Pointcut("execution(public * *(..)) "
            + "&& @annotation(outboxPayload)")
    public void anyOutboxEventOperation(OutboxOperation outboxPayload) {
    }
}
