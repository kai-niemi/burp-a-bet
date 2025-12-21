package io.cockroachdb.betting.common.aspect;

import java.lang.reflect.Method;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.resilience.retry.MethodRetryPredicate;

public class TransactionRetryPredicate implements MethodRetryPredicate {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static final String SERIALIZATION_FAILURE = "40001";

    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean shouldRetry(Method method, Throwable ex) {
        if (!isEnabled() || ex == null) {
            return false;
        }

        boolean transientError = false;

        Throwable throwable = NestedExceptionUtils.getMostSpecificCause(ex);
        if (throwable instanceof SQLException sqlException) {
            if (SERIALIZATION_FAILURE.equals(sqlException.getSQLState())) {
                transientError = true;
            }

            if (transientError) {
                logger.warn("Transient SQL exception detected : sql state [{}], message [{}]",
                        sqlException.getSQLState(), ex.toString());
            }

        }

        logger.warn("Non-transient exception {}", ex.getClass());

        return transientError;
    }
}
