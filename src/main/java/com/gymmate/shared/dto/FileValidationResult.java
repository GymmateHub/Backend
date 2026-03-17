package com.gymmate.shared.dto;

import java.util.List;

public record FileValidationResult(boolean valid, List<String> errors) {}
