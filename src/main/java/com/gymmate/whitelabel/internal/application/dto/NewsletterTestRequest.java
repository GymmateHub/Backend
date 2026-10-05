package com.gymmate.whitelabel.internal.application.dto;

import com.gymmate.whitelabel.internal.domain.NewsletterProvider;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewsletterTestRequest {

    private NewsletterProvider newsletterProvider;

    @NotBlank(message = "API key is required")
    private String newsletterApiKey;

    private String newsletterListId;
}
