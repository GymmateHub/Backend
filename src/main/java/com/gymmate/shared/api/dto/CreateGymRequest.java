package com.gymmate.shared.api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * Request DTO for creating a new gym within an organisation.
 */
@Data
public class CreateGymRequest {

    @NotBlank(message = "Gym name is required")
    @Size(min = 2, max = 100, message = "Gym name must be between 2 and 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotBlank(message = "Contact email is required")
    @Email(message = "Contact email must be a valid email address")
    private String contactEmail;

    @NotBlank(message = "Contact phone is required")
    @Size(max = 20, message = "Contact phone must not exceed 20 characters")
    private String contactPhone;

    // Optional address fields
    private String address;
    private String city;
    private String state;
    private String country;
    private String postalCode;

    // Optional settings
    private String timezone;
    private String currency;
}

