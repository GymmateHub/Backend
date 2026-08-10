package com.gymmate.whitelabel.api.dto;

import com.gymmate.whitelabel.domain.NewsletterProvider;
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
