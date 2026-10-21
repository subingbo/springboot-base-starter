package com.campus.common.annotation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 轻量级自由文本校验：拒绝明显的脚本/事件处理器载荷，作为 JSON API 的纵深防御。
 */
@Documented
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeTextValidator.class)
public @interface SafeText {

    String message() default "内容包含非法字符";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
