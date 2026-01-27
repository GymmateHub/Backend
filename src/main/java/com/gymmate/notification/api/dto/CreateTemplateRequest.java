package com.gymmate.notification.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

/**
 * Request DTO for creating a newsletter template.
 */
@Data
public class CreateTemplateRequest {

    private UUID gymId;

    @NotBlank(message = "Template name is required")
    @Size(max = 100, message = "Template name must be less than 100 characters")
    private String name;

    @NotBlank(message = "Subject is required")
    @Size(max = 255, message = "Subject must be less than 255 characters")
    private String subject;

    @NotBlank(message = "Body is required")
    private String body;

    private String templateType = "EMAIL";

    private String placeholders = "[]";
}
