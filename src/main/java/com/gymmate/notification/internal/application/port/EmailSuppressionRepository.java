package com.gymmate.notification.internal.application.port;

import com.gymmate.notification.internal.domain.EmailSuppression;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository interface for EmailSuppression domain entity.
 */
public interface EmailSuppressionRepository {

    EmailSuppression save(EmailSuppression suppression);

    Optional<EmailSuppression> findById(UUID id);

    Optional<EmailSuppression> findByEmailIgnoreCaseAndActiveTrue(String email);

    List<EmailSuppression> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndActiveTrue(String email);

    void deleteById(UUID id);
    
    List<EmailSuppression> saveAll(Iterable<EmailSuppression> entities);
    
    boolean existsById(UUID id);
    
    List<EmailSuppression> findAll();
    
    List<EmailSuppression> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void delete(EmailSuppression entity);
    
    void deleteAll(Iterable<EmailSuppression> entities);
    
    EmailSuppression saveAndFlush(EmailSuppression entity);
    
    void flush();
    
    Page<EmailSuppression> findAll(Pageable pageable);
}
