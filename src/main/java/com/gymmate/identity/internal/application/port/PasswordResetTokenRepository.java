package com.gymmate.identity.internal.application.port;

import com.gymmate.identity.internal.domain.PasswordResetToken;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository for password reset tokens.
 */
public interface PasswordResetTokenRepository {
    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser_Id(UUID userId);
    
    PasswordResetToken save(PasswordResetToken entity);
    
    List<PasswordResetToken> saveAll(Iterable<PasswordResetToken> entities);
    
    Optional<PasswordResetToken> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<PasswordResetToken> findAll();
    
    List<PasswordResetToken> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(PasswordResetToken entity);
    
    void deleteAll(Iterable<PasswordResetToken> entities);
    
    PasswordResetToken saveAndFlush(PasswordResetToken entity);
    
    void flush();
    
    Page<PasswordResetToken> findAll(Pageable pageable);
}
