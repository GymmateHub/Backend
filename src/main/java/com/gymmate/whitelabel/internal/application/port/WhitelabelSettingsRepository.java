package com.gymmate.whitelabel.internal.application.port;

import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface WhitelabelSettingsRepository {
    WhitelabelSettings save(WhitelabelSettings settings);
    Optional<WhitelabelSettings> findById(UUID id);
    Optional<WhitelabelSettings> findByOrganisationIdAndGymIdIsNull(UUID organisationId);
    Optional<WhitelabelSettings> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);
    Optional<WhitelabelSettings> findByGymId(UUID gymId);
    
    List<WhitelabelSettings> saveAll(Iterable<WhitelabelSettings> entities);
    
    boolean existsById(UUID id);
    
    List<WhitelabelSettings> findAll();
    
    List<WhitelabelSettings> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(WhitelabelSettings entity);
    
    void deleteAll(Iterable<WhitelabelSettings> entities);
    
    WhitelabelSettings saveAndFlush(WhitelabelSettings entity);
    
    void flush();
    
    Page<WhitelabelSettings> findAll(Pageable pageable);
}
