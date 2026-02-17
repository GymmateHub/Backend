package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.InviteStatus;
import com.gymmate.user.domain.UserInvite;
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
