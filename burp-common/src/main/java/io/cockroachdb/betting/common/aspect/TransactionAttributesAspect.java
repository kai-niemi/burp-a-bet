package io.cockroachdb.betting.common.aspect;

import java.lang.annotation.Annotation;

import javax.sql.DataSource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.Assert;

import io.cockroachdb.betting.common.annotations.FollowerRead;
import io.cockroachdb.betting.common.annotations.FollowerReadMode;
import io.cockroachdb.betting.common.annotations.TransactionBoundary;
import io.cockroachdb.betting.common.annotations.TransactionPriority;

/**
 * AOP aspect that sets specific and arbitrary transaction/session variables.
 * <p>
 * The main pre-condition is that there must be an existing transaction in scope.
 * <p>
 * See {@link org.springframework.transaction.annotation.EnableTransactionManagement} for
 * controlling weaving order.
 *
 * @author Kai Niemi
 */
@Aspect
@Order(TransactionAttributesAspect.PRECEDENCE)
public class TransactionAttributesAspect {
    static <A extends Annotation> A findAnnotation(ProceedingJoinPoint pjp, Class<A> annotationType) {
        return AnnotationUtils.findAnnotation(pjp.getSignature().getDeclaringType(), annotationType);
    }

    /**
     * The precedence at which this advice is ordered by which also controls
     * the order it is invoked in the call chain between a source and target.
     */
    public static final int PRECEDENCE = AdvisorOrder.TRANSACTION_CONTEXT_ADVISOR;

    private final JdbcTemplate jdbcTemplate;

    public TransactionAttributesAspect(@Autowired DataSource dataSource) {
        Assert.notNull(dataSource, "dataSource is null");
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    @Around(value = "io.cockroachdb.betting.common.aspect.Pointcuts.anyTransactionBoundaryOperation(transactionBoundary)",
            argNames = "pjp,transactionBoundary")
    public Object doInTransaction(ProceedingJoinPoint pjp, TransactionBoundary transactionBoundary)
            throws Throwable {
        Assert.isTrue(TransactionSynchronizationManager.isActualTransactionActive(),
                "Expecting active transaction - check advice @Order and @EnableTransactionManagement order");

        // Grab from type if needed (for non-annotated methods)
        if (transactionBoundary == null) {
            transactionBoundary = findAnnotation(pjp, TransactionBoundary.class);
        }

        Assert.notNull(transactionBoundary, "No @TransactionBoundary annotation found!?");

        if (!"(empty)".equals(transactionBoundary.applicationName())) {
            jdbcTemplate.update("SET application_name=?", transactionBoundary.applicationName());
        }

        if (!TransactionPriority.NORMAL.equals(transactionBoundary.priority())) {
            jdbcTemplate.execute("SET TRANSACTION PRIORITY "
                                 + transactionBoundary.retryPriority().name());
        }

        if (!"0s".equals(transactionBoundary.idleTimeout())) {
            jdbcTemplate.update("SET idle_in_transaction_session_timeout=?", transactionBoundary.idleTimeout());
        }

        if (transactionBoundary.readOnly()) {
            jdbcTemplate.execute("SET transaction_read_only=true");
        }

        FollowerRead followerRead = transactionBoundary.timeTravel();

        if (followerRead.mode().equals(FollowerReadMode.EXACT_STALENESS_READ)) {
            Assert.isTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly(),
                    "Expecting read-only transaction - check @Transactional readonly attribute: "
                    + pjp.getSignature().toShortString());

            if ("0s".equals(followerRead.interval())) {
                jdbcTemplate.execute("SET TRANSACTION AS OF SYSTEM TIME follower_read_timestamp()");
            } else {
                jdbcTemplate.execute("SET TRANSACTION AS OF SYSTEM TIME INTERVAL '"
                                     + followerRead.interval() + "'");
            }
        } else if (followerRead.mode().equals(FollowerReadMode.BOUNDED_STALENESS_READ)) {
            Assert.isTrue(TransactionSynchronizationManager.isCurrentTransactionReadOnly(),
                    "Expecting read-only transaction - check @Transactional readonly attribute: "
                    + pjp.getSignature().toShortString());

            jdbcTemplate.execute("SET TRANSACTION AS OF SYSTEM TIME with_max_staleness('"
                                 + followerRead.interval() + "')");
        } else {
            throw new UnsupportedOperationException("Not a supported followerRead type");
        }

        return pjp.proceed();
    }
}
