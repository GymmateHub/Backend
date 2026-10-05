package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.InviteStatus;
import com.gymmate.identity.internal.domain.UserInvite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserInviteRepository extends JpaRepository<UserInvite, UUID> {

    Optional<UserInvite> findByToken(String token);

    Optional<UserInvite> findByTokenHash(String tokenHash);

    List<UserInvite> findByGymId(UUID gymId);

    List<UserInvite> findByEmailAndGymId(String email, UUID gymId);

    List<UserInvite> findByStatusAndExpiresAtBefore(InviteStatus status, LocalDateTime dateTime);
}
