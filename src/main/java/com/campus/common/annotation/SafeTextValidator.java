package com.campus.common.annotation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class SafeTextValidator implements ConstraintValidator<SafeText, String> {

    private static final Pattern DANGEROUS = Pattern.compile(
            "(?i)(<\\s*script|javascript\\s*:|<\\s*iframe|<\\s*object|<\\s*embed|on\\w+\\s*=)");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            return true;
        }
        return !DANGEROUS.matcher(value).find();
    }
}
