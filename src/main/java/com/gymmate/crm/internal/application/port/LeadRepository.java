package com.gymmate.crm.internal.application.port;

import com.gymmate.crm.internal.domain.Lead;
import com.gymmate.crm.internal.domain.LeadStatus;

import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface LeadRepository {
    List<Lead> findByGymId(UUID gymId);
    List<Lead> findByOrganisationId(UUID organisationId);
    List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status);
    List<Lead> findByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);
    long countByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);
    
    Lead save(Lead entity);
    
    List<Lead> saveAll(Iterable<Lead> entities);
    
    Optional<Lead> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<Lead> findAll();
    
    List<Lead> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(Lead entity);
    
    void deleteAll(Iterable<Lead> entities);
    
    Lead saveAndFlush(Lead entity);
    
    void flush();
    
    Page<Lead> findAll(Pageable pageable);
}
