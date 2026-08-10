package com.gymmate.scheduling.api.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateBookingRequest(
    @NotNull UUID gymId,
    @NotNull UUID memberId,
    @NotNull UUID scheduleId,
    String memberNotes
) {}

