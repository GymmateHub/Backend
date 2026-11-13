package com.gymmate.configuration.api.dto;

/**
 * Resolved and rendered email template ready for delivery.
 *
 * @param templateKey       Unique template identifier (e.g. {@code EMAIL_PASSWORD_RESET})
 * @param subject           Rendered email subject with placeholders interpolated
 * @param htmlBody          Rendered HTML body when configured in the database or inline default
 * @param classpathTemplate Optional Thymeleaf classpath template name when using the default file template
 * @param bodyCustomised    Whether the HTML body has been customised from the classpath default
 * @param enabled           Whether sending this email template is enabled
 */
public record RenderedEmail(
        String templateKey,
        String subject,
        String htmlBody,
        String classpathTemplate,
        boolean bodyCustomised,
        boolean enabled
) {
}

