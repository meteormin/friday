package com.meteormin.friday.aspect.annotation;

import java.lang.annotation.*;

/**
 * 로그인 히스토리를 저장하기 위한 LoginEndPoint 지정
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginEndPoint {
}
