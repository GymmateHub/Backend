package com.gymmate.notification.application.channel;

import com.gymmate.notification.domain.NotificationChannel;

/**
 * Exception thrown when a notification channel fails to send.
 */
public class ChannelException extends Exception {

    private final NotificationChannel channel;

    public ChannelException(NotificationChannel channel, String message) {
        super(message);
        this.channel = channel;
    }

    public ChannelException(NotificationChannel channel, String message, Throwable cause) {
        super(message, cause);
        this.channel = channel;
    }

    public NotificationChannel getChannel() {
        return channel;
    }
}
