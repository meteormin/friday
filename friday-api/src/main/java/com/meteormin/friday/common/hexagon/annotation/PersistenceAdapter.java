package com.meteormin.friday.common.hexagon.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * Persistence Adapter. for persistence layer
 *
 * @author meteormin
 * @since 2023/09/09
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Transactional
public @interface PersistenceAdapter {

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    String[] label() default {};

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    String timeoutString() default "";

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    boolean readOnly() default false;

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] rollbackFor() default {};

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    String[] rollbackForClassName() default {};

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] noRollbackFor() default {};

    /**
     * {@inheritDoc}
     */
    @AliasFor(annotation = Transactional.class)
    String[] noRollbackForClassName() default {};
}
