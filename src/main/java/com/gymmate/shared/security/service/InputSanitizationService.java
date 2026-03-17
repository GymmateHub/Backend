package com.gymmate.shared.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

// src/main/java/com/gymmate/shared/security/service/InputSanitizationService.java
@Service
@Slf4j
public class InputSanitizationService {

  private static final Pattern SCRIPT_PATTERN = Pattern.compile(
    "<script[^>]*>.*?</script>", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);

  private static final Pattern JAVASCRIPT_PATTERN = Pattern.compile(
    "javascript:", Pattern.CASE_INSENSITIVE);

  private static final Pattern ON_EVENT_PATTERN = Pattern.compile(
    "on\\w+\\s*=", Pattern.CASE_INSENSITIVE);

  private static final Pattern HTML_TAG_PATTERN = Pattern.compile(
    "<[^>]*>", Pattern.DOTALL);

  public String sanitizeInput(String input) {
    if (input == null || input.trim().isEmpty()) {
      return input;
    }

    // Remove script tags
    String sanitized = SCRIPT_PATTERN.matcher(input).replaceAll("");

    // Remove javascript: protocol
    sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");

    // Remove on* event handlers
    sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");

    // Remove HTML tags (optional - use with caution)
    sanitized = HTML_TAG_PATTERN.matcher(sanitized).replaceAll("");

    // Normalize whitespace
    sanitized = sanitized.replaceAll("\\s+", " ").trim();

    return sanitized;
  }

  public String sanitizeHtml(String input, boolean allowBasicFormatting) {
    if (input == null || input.trim().isEmpty()) {
      return input;
    }

    if (!allowBasicFormatting) {
      return sanitizeInput(input);
    }

    // Allow basic HTML tags only
    String sanitized = input;

    // Remove dangerous tags and attributes
    sanitized = SCRIPT_PATTERN.matcher(sanitized).replaceAll("");
    sanitized = JAVASCRIPT_PATTERN.matcher(sanitized).replaceAll("");
    sanitized = ON_EVENT_PATTERN.matcher(sanitized).replaceAll("");

    // Remove dangerous attributes
    sanitized = sanitized.replaceAll("(?i)on\\w+\\s*=", "");
    sanitized = sanitized.replaceAll("(?i)style\\s*=", "");
    sanitized = sanitized.replaceAll("(?i)class\\s*=", "");

    return sanitized;
  }

  public boolean containsXss(String input) {
    if (input == null) return false;

    String lowerInput = input.toLowerCase();

    // Check for common XSS patterns
    return lowerInput.contains("<script") ||
      lowerInput.contains("javascript:") ||
      lowerInput.contains("onerror") ||
      lowerInput.contains("onload") ||
      lowerInput.contains("onclick") ||
      lowerInput.contains("onmouseover") ||
      lowerInput.contains("<iframe") ||
      lowerInput.contains("<object") ||
      lowerInput.contains("<embed") ||
      lowerInput.contains("eval(") ||
      lowerInput.contains("alert(") ||
      lowerInput.contains("document.cookie");
  }

  public String sanitizeFilename(String filename) {
    if (filename == null) return null;

    // Remove path traversal attempts
    String sanitized = filename.replaceAll("\\.\\.", "").replaceAll("/", "").replaceAll("\\\\", "");

    // Remove dangerous characters
    sanitized = sanitized.replaceAll("[^a-zA-Z0-9._-]", "");

    // Limit length
    if (sanitized.length() > 255) {
      sanitized = sanitized.substring(0, 255);
    }

    return sanitized.trim();
  }
}
