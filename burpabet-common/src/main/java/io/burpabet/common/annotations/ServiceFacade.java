package io.burpabet.common.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.stereotype.Service;

/**
 * Indicates the annotated class is a coarse-grained service facade and transaction boundary.
 * Its architectural role is to delegate to control services or repositories to perform
 * actual business logic processing in the context of a new transaction.
 * <p/>
 * Marks the annotated class as {@link org.springframework.transaction.annotation.Transactional @Transactional}
 * with propagation level {@link org.springframework.transaction.annotation.Propagation#REQUIRES_NEW REQUIRES_NEW},
 * clearly indicating that a new transaction is started before method entry.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Service
@TransactionBoundary
public @interface ServiceFacade {
}
