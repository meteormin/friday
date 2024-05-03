package com.meteormin.friday.common.hexagon.annotation;

import org.springframework.core.annotation.AliasFor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.*;

/**
 * [description]
 *
 * @author meteormin
 * @since 2023/09/09
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@RestController
@RequestMapping
public @interface RestAdapter {

    @AliasFor(annotation = RestController.class)
    String value() default "";

    @AliasFor(annotation = RequestMapping.class, attribute = "path") // (2)
    String path() default "";
}
