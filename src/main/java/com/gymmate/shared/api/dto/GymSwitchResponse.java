package com.gymmate.shared.api.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

/**
 * Response DTO for gym switch operation.
 * Returns new JWT tokens with the selected gym context.
 */
@Data
@Builder
public class GymSwitchResponse {

    private UUID gymId;
    private String gymName;
    private UUID organisationId;

    private String accessToken;
    private String refreshToken;

    private String message;
}

