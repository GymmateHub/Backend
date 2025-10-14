package com.gymmate.user.infrastructure;

import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository adapter that implements the domain UserRepository interface
 * using Spring Data JPA.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;

    @Override
    public User save(User user) {
        return jpaRepository.save(user);
    }

    @Override
    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByEmailAndGymId(email, currentGymId) :
            jpaRepository.findByEmail(email);
    }

    @Override
    public Optional<User> findByEmailAndGymId(String email, String gymId) {
        return jpaRepository.findByEmailAndGymId(email, UUID.fromString(gymId));
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    @Override
    public List<User> findByRole(UserRole role) {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByRoleAndGymId(role, currentGymId) :
            jpaRepository.findByRole(role);
    }

    @Override
    public List<User> findByStatus(UserStatus status) {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByStatusAndGymId(status, currentGymId) :
            jpaRepository.findByStatus(status);
    }

    @Override
    public List<User> findAll() {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByGymId(currentGymId) :
            jpaRepository.findAll();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
