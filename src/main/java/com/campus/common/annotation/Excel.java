package com.campus.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Excel {

    String name();

    int order() default 0;

    int width() default 20;

    String dictType() default "";

    String dateFormat() default "yyyy-MM-dd HH:mm:ss";

    boolean required() default false;
}
