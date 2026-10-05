package com.gymmate.identity.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import com.gymmate.identity.internal.domain.User;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entity for password reset tokens.
 */
@Getter
@Setter
@NoArgsConstructor
public class PasswordResetToken extends BaseAuditEntity {

    private String token;

    private User user;

    private LocalDateTime expiryDate;

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate);
    }

    public static PasswordResetToken create(User user, String token, int expirationMinutes) {
        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiryDate(LocalDateTime.now().plusMinutes(expirationMinutes));
        return resetToken;
    }
}
