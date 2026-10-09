package com.gymmate.scheduling.internal.application.dto;

import java.util.UUID;

public record CreateAreaRequest(
    UUID gymId,
    String name,
    String areaType,
    Integer capacity
) {
}
