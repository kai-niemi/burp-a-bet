package io.cockroachdb.betting.common.annotations;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * A meta-annotation including @Transactional, indicating that the annotated class or method
 * must always execute within an existing transaction context, hence with Propagation.MANDATORY.
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Transactional(propagation = Propagation.MANDATORY)
public @interface TransactionMandatory {
}
