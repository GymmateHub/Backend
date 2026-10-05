package com.gymmate.notification.internal.application.port;

import com.gymmate.notification.internal.domain.SnsProcessedMessage;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface SnsProcessedMessageRepository {

    Optional<SnsProcessedMessage> findByMessageId(String messageId);

    boolean existsByMessageId(String messageId);
    
    SnsProcessedMessage save(SnsProcessedMessage entity);
    
    List<SnsProcessedMessage> saveAll(Iterable<SnsProcessedMessage> entities);
    
    Optional<SnsProcessedMessage> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<SnsProcessedMessage> findAll();
    
    List<SnsProcessedMessage> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(SnsProcessedMessage entity);
    
    void deleteAll(Iterable<SnsProcessedMessage> entities);
    
    SnsProcessedMessage saveAndFlush(SnsProcessedMessage entity);
    
    void flush();
    
    Page<SnsProcessedMessage> findAll(Pageable pageable);
}
