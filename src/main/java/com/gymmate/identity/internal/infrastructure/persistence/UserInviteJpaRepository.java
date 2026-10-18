package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.InviteStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserInviteJpaRepository extends JpaRepository<UserInviteJpaEntity, UUID> {

    Optional<UserInviteJpaEntity> findByToken(String token);

    Optional<UserInviteJpaEntity> findByTokenHash(String tokenHash);

    List<UserInviteJpaEntity> findByGymId(UUID gymId);

    List<UserInviteJpaEntity> findByEmailAndGymId(String email, UUID gymId);

    List<UserInviteJpaEntity> findByStatusAndExpiresAtBefore(InviteStatus status, LocalDateTime dateTime);
}
