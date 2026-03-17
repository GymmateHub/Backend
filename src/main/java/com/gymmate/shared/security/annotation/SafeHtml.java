package com.gymmate.shared.security.annotation;

import com.gymmate.shared.security.service.InputSanitizationService;
import jakarta.validation.Constraint;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Payload;
import org.springframework.beans.factory.annotation.Autowired;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// src/main/java/com/gymmate/shared/security/annotation/SafeHtml.java
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SafeHtmlValidator.class)
public @interface SafeHtml {
  boolean allowBasicFormatting() default false;
  String message() default "Input contains unsafe HTML content";
  Class<?>[] groups() default {};
  Class<? extends Payload>[] payload() default {};
}

