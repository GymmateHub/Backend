package com.gymmate.notification.api.spi;

import java.util.UUID;

/** Member and user info combined for newsletter/broadcast delivery. */
public record MemberRecipient(
        UUID memberId,
        UUID userId,
        String firstName,
        String lastName,
        String email) {
}
