package com.gymmate.shared.security.annotation;

import com.gymmate.shared.security.service.InputSanitizationService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;

public class SafeHtmlValidator implements ConstraintValidator<SafeHtml, String> {

  @Autowired
  private InputSanitizationService sanitizationService;

  private boolean allowBasicFormatting;

  @Override
  public void initialize(SafeHtml constraintAnnotation) {
    this.allowBasicFormatting = constraintAnnotation.allowBasicFormatting();
  }

  @Override
  public boolean isValid(String value, ConstraintValidatorContext context) {
    if (value == null) return true;

    String sanitized = sanitizationService.sanitizeHtml(value, allowBasicFormatting);
    return sanitized.equals(value); // If equal, no dangerous content was removed
  }
}
