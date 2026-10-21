package com.campus.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String key() default "";

    int limit() default 10;

    int windowSeconds() default 60;

    Dimension dimension() default Dimension.IP;

    enum Dimension {
        IP, USER
    }
}
