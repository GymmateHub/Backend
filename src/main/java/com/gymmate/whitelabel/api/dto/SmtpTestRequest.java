package com.gymmate.whitelabel.api.dto;

import com.gymmate.whitelabel.domain.SmtpSecurity;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmtpTestRequest {
    @NotBlank(message = "SMTP host is required")
    private String smtpHost;

    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPassword;
    private SmtpSecurity smtpSecurity;
    private String fromEmail;

    @NotBlank(message = "Recipient test email is required")
    private String recipientEmail;
}
