package com.gymmate.gym.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GymUpdateRequest(
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
    String contactPhone,

    @Size(max = 500, message = "Website URL must not exceed 500 characters")
    String website,

    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    String logoUrl,

    @Size(max = 50, message = "Timezone must not exceed 50 characters")
    String timezone,

    @Size(min = 3, max = 3, message = "Currency must be a 3-letter code (e.g., USD, EUR)")
    String currency,

    Integer maxMembers
) {}
