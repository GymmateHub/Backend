package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;
import com.gymmate.identity.internal.domain.PasswordResetToken;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link PasswordResetToken} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "PasswordResetToken")
@Table(name = "password_reset_tokens")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(PasswordResetToken.class)
public class PasswordResetTokenJpaEntity extends BaseAuditJpaEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserJpaEntity user;

    @Column(nullable = false)
    private LocalDateTime expiryDate;
}
