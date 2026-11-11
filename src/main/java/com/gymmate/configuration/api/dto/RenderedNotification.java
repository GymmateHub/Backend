package com.gymmate.configuration.api.dto;

/**
 * Resolved and rendered notification message ready for persistence and broadcast.
 *
 * @param templateKey Unique notification template/event key (e.g. {@code SUBSCRIPTION_EXPIRED})
 * @param title       Rendered notification title with placeholders interpolated
 * @param message     Rendered notification message with placeholders interpolated
 * @param enabled     Whether this notification is enabled
 */
public record RenderedNotification(
        String templateKey,
        String title,
        String message,
        boolean enabled
) {
}

