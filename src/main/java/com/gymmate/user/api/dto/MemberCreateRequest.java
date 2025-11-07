package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO for creating a new member.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberCreateRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String membershipNumber;
}

