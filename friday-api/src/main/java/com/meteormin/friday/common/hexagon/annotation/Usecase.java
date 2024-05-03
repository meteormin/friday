package com.meteormin.friday.common.hexagon.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.lang.annotation.*;

/**
 * usecase.
 *
 * @author meteormin
 * @since 2023/09/09
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
@Transactional
public @interface Usecase {

    @AliasFor(annotation = Component.class)
    String value() default "";

    @AliasFor(annotation = Transactional.class)
    String[] label() default {};

    @AliasFor(annotation = Transactional.class)
    Propagation propagation() default Propagation.REQUIRED;

    @AliasFor(annotation = Transactional.class)
    Isolation isolation() default Isolation.DEFAULT;

    @AliasFor(annotation = Transactional.class)
    int timeout() default TransactionDefinition.TIMEOUT_DEFAULT;

    @AliasFor(annotation = Transactional.class)
    String timeoutString() default "";

    @AliasFor(annotation = Transactional.class)
    boolean readOnly() default false;

    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] rollbackFor() default {};

    @AliasFor(annotation = Transactional.class)
    String[] rollbackForClassName() default {};

    @AliasFor(annotation = Transactional.class)
    Class<? extends Throwable>[] noRollbackFor() default {};

    @AliasFor(annotation = Transactional.class)
    String[] noRollbackForClassName() default {};
}
