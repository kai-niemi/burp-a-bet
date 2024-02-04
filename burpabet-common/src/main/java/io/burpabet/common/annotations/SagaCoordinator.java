package io.burpabet.common.annotations;

import java.lang.annotation.*;

/**
 * Indicates the annotated class is a Saga Coordinator (SEC).
 */
@Inherited
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface SagaCoordinator {
    String value();
}
