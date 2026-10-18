package com.gymmate.whitelabel.internal.application.dto;

import com.gymmate.whitelabel.internal.domain.SmtpSecurity;
import jakarta.validation.constraints.NotBlank;

public record SmtpTestRequest(
        @NotBlank(message = "SMTP host is required")
        String smtpHost,
        Integer smtpPort,
        String smtpUsername,
        String smtpPassword,
        SmtpSecurity smtpSecurity,
        String fromEmail,
        @NotBlank(message = "Recipient test email is required")
        String recipientEmail
) {
}
