package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for User entity.
 */
@Repository
public interface UserJpaRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    Optional<User> findByEmailAndGymId(String email, UUID gymId);

    boolean existsByEmail(String email);

    List<User> findByRole(UserRole role);

    List<User> findByStatus(UserStatus status);

    List<User> findByGymId(UUID gymId);

    List<User> findByRoleAndGymId(UserRole role, UUID gymId);

    List<User> findByStatusAndGymId(UserStatus status, UUID gymId);
}
