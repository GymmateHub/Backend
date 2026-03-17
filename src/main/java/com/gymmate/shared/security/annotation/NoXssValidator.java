package com.gymmate.shared.security.annotation;

import com.gymmate.shared.security.service.InputSanitizationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

// Validator implementation
public class NoXssValidator implements ConstraintValidator<NoXss, String> {

  @Autowired
  private InputSanitizationService sanitizationService;

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true;
    return !sanitizationService.containsXss(value);
  }
}
