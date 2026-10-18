package com.gymmate.scheduling.internal.application.dto;

import java.util.UUID;

public record CreateCategoryRequest(
    UUID gymId,
    String name,
    String description,
    String color
) {
}
