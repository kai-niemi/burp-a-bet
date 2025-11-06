package io.cockroachdb.betting.common.aspect;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.cockroachdb.betting.common.annotations.OutboxOperation;
import io.cockroachdb.betting.common.outbox.OutboxRepository;

@Aspect
@Order(OutboxAspect.PRECEDENCE)
public class OutboxAspect {
    public static final int PRECEDENCE = AdvisorOrder.CHANGE_FEED_ADVISOR;

    private final OutboxRepository outboxRepository;

    public OutboxAspect(OutboxRepository outboxRepository) {
        this.outboxRepository = outboxRepository;
    }

    @AfterReturning(pointcut = "io.cockroachdb.betting.common.aspect.Pointcuts.anyOutboxEventOperation(outboxOperation)",
            returning = "returnValue", argNames = "returnValue,outboxOperation")
    public void doAfterOutboxOperation(Object returnValue, OutboxOperation outboxOperation) {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expected existing transaction - check advisor @Order");

        outboxRepository.writeEvent(returnValue, outboxOperation.aggregateType());
    }
}

