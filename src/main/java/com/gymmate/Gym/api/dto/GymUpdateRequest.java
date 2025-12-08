package com.gymmate.Gym.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GymUpdateRequest(
    @NotBlank(message = "Gym name is required")
    @Size(max = 255, message = "Gym name must not exceed 255 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String contactEmail,

    @NotBlank(message = "Contact phone is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String contactPhone
) {}
