package com.gymmate.shared.validation;

import com.gymmate.shared.security.InputSanitizationService;
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
