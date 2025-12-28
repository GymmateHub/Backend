package com.gymmate.gym.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

/**
 * DTO for gym registration requests.
 */
public record GymRegistrationRequest(
    @NotNull(message = "Owner ID is required")
    UUID ownerId,

    @NotBlank(message = "Gym name is required")
    @Size(max = 255, message = "Gym name must not exceed 255 characters")
    String name,

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    String description,

    @Size(max = 255, message = "Street address must not exceed 255 characters")
    String street,

    @Size(max = 100, message = "City must not exceed 100 characters")
    String city,

    @Size(max = 100, message = "State must not exceed 100 characters")
    String state,

    @Size(max = 20, message = "Postal code must not exceed 20 characters")
    String postalCode,

    @Size(max = 100, message = "Country must not exceed 100 characters")
    String country,

    @NotBlank(message = "Contact email is required")
    @Email(message = "Invalid email format")
    @Size(max = 255, message = "Email must not exceed 255 characters")
    String contactEmail,

    @NotBlank(message = "Contact phone is required")
    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String contactPhone
) {}
