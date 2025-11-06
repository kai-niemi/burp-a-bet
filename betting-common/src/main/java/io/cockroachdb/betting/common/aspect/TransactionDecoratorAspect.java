package io.cockroachdb.betting.common.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import io.cockroachdb.betting.common.annotations.TimeTravel;
import io.cockroachdb.betting.common.annotations.TimeTravelMode;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;
import io.cockroachdb.betting.common.annotations.TransactionPriority;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

/**
 * AOP aspect that sets specific and arbitrary transaction/session variables.
 * <p>
 * The main pre-condition is that there must be an existing transaction in scope.
 * This advice must be applied after the {@link TransactionRetryAspect} if used simultaneously,
 * and the Spring transaction advisor in the call chain.
 * <p>
 * See {@link org.springframework.transaction.annotation.EnableTransactionManagement} for
 * controlling weaving order.
 *
 * @author Kai Niemi
 */
@Aspect
@Order(TransactionDecoratorAspect.PRECEDENCE)
public class TransactionDecoratorAspect {
    /**
     * The precedence at which this advice is ordered by which also controls
     * the order it is invoked in the call chain between a source and target.
     */
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_ATTRIBUTES_ADVISOR;

    private final JdbcTemplate jdbcTemplate;

    public TransactionDecoratorAspect(@Autowired JdbcTemplate jdbcTemplate) {
        Assert.notNull(jdbcTemplate, "jdbcTemplate is null");
        this.jdbcTemplate = jdbcTemplate;
    }

    @Around(value = "io.cockroachdb.betting.common.aspect.Pointcuts.anyTransactionBoundaryOperation(transactionBoundary)",
            argNames = "pjp,transactionBoundary")
    public Object doInTransaction(ProceedingJoinPoint pjp, TransactionBoundary transactionBoundary)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting active transaction - check advice @Order and @EnableTransactionManagement order");

        // Grab from type if needed (for non-annotated methods)
        if (transactionBoundary == null) {
            transactionBoundary = TransactionRetryAspect.findAnnotation(pjp, TransactionBoundary.class);
        }

        Assert.notNull(transactionBoundary, "No @TransactionBoundary annotation found!?");

        if (!"(empty)".equals(transactionBoundary.applicationName())) {
            jdbcTemplate.update("SET application_name=?", transactionBoundary.applicationName());
        }

        if (!TransactionPriority.NORMAL.equals(transactionBoundary.retryPriority())) {
            if (TransactionSynchronizationManager.hasResource(TransactionRetryAspect.RETRY_ASPECT_CALL_COUNT)) {
                Integer numCalls = (Integer) TransactionSynchronizationManager
                        .getResource(TransactionRetryAspect.RETRY_ASPECT_CALL_COUNT);
                if (numCalls > 1) {
                    jdbcTemplate.execute("SET TRANSACTION PRIORITY "
                            + transactionBoundary.retryPriority().name());
                }
            }
        } else if (!TransactionPriority.NORMAL.equals(transactionBoundary.priority())) {
            jdbcTemplate.execute("SET TRANSACTION PRIORITY "
                    + transactionBoundary.priority().name());
        }

        if (!"0s".equals(transactionBoundary.idleTimeout())) {
            jdbcTemplate.update("SET idle_in_transaction_session_timeout=?", transactionBoundary.idleTimeout());
        }

        if (transactionBoundary.readOnly()) {
            jdbcTemplate.execute("SET transaction_read_only=true");
        }

        TimeTravel timeTravel = transactionBoundary.timeTravel();

        if (timeTravel.mode().equals(TimeTravelMode.FOLLOWER_READ)) {
            jdbcTemplate.execute("SET TRANSACTION AS OF SYSTEM TIME follower_read_timestamp()");
        } else if (timeTravel.mode().equals(TimeTravelMode.HISTORICAL_READ)) {
            jdbcTemplate.update("SET TRANSACTION AS OF SYSTEM TIME INTERVAL '"
                    + timeTravel.interval() + "'");
        }

        return pjp.proceed();
    }
}
