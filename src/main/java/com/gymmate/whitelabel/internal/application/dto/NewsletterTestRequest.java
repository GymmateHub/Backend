package com.gymmate.whitelabel.internal.application.dto;

import com.gymmate.whitelabel.internal.domain.NewsletterProvider;
import jakarta.validation.constraints.NotBlank;

public record NewsletterTestRequest(
        NewsletterProvider newsletterProvider,
        @NotBlank(message = "API key is required")
        String newsletterApiKey,
        String newsletterListId
) {
}
