package com.gymmate.identity.internal.application.port;

import com.gymmate.shared.constants.InviteStatus;
import com.gymmate.identity.internal.domain.UserInvite;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface UserInviteRepository {

    Optional<UserInvite> findByToken(String token);

    Optional<UserInvite> findByTokenHash(String tokenHash);

    List<UserInvite> findByGymId(UUID gymId);

    List<UserInvite> findByEmailAndGymId(String email, UUID gymId);

    List<UserInvite> findByStatusAndExpiresAtBefore(InviteStatus status, LocalDateTime dateTime);
    
    UserInvite save(UserInvite entity);
    
    List<UserInvite> saveAll(Iterable<UserInvite> entities);
    
    Optional<UserInvite> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<UserInvite> findAll();
    
    List<UserInvite> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(UserInvite entity);
    
    void deleteAll(Iterable<UserInvite> entities);
    
    UserInvite saveAndFlush(UserInvite entity);
    
    void flush();
    
    Page<UserInvite> findAll(Pageable pageable);
}
