package com.gymmate.notification.application.channel;

import com.gymmate.notification.domain.NotificationChannel;

/**
 * Interface for channel-specific notification senders.
 */
public interface ChannelSender {

    /**
     * Get the channel this sender handles.
     */
    NotificationChannel getChannel();

    /**
     * Send a notification via this channel.
     *
     * @param recipient The recipient (email, phone number, etc.)
     * @param subject   Subject line (for email) or title
     * @param body      The message body (HTML for email, plain text for SMS)
     * @throws ChannelException if sending fails
     */
    void send(String recipient, String subject, String body) throws ChannelException;
}
