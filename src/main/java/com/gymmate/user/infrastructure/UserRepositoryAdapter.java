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
 * Repository adapter that provides tenant-aware delegation to the JPA repository.
 */
@Component
@RequiredArgsConstructor
public class UserRepositoryAdapter {

    private final UserJpaRepository jpaRepository;

    public User save(User user) {
        return jpaRepository.save(user);
    }

    public Optional<User> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    public Optional<User> findByEmail(String email) {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByEmailAndGymId(email, currentGymId) :
            jpaRepository.findByEmail(email);
    }

    public Optional<User> findByEmailAndGymId(String email, UUID gymId) {
        return jpaRepository.findByEmailAndGymId(email, gymId);
    }

    public boolean existsByEmail(String email) {
        return jpaRepository.existsByEmail(email);
    }

    public List<User> findByRole(UserRole role) {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByRoleAndGymId(role, currentGymId) :
            jpaRepository.findByRole(role);
    }

    public List<User> findByStatus(UserStatus status) {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByStatusAndGymId(status, currentGymId) :
            jpaRepository.findByStatus(status);
    }

    public List<User> findAll() {
        UUID currentGymId = TenantContext.getCurrentTenantId();
        return currentGymId != null ?
            jpaRepository.findByGymId(currentGymId) :
            jpaRepository.findAll();
    }

    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }
}
