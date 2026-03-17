package com.gymmate.shared.security.aspect;

import com.gymmate.shared.constants.AuditEventType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuditLog {
  AuditEventType eventType();
  String message() default "";
}
