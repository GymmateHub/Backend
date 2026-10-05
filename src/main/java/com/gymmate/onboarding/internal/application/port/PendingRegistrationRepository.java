package com.gymmate.onboarding.internal.application.port;

import com.gymmate.onboarding.internal.domain.PendingRegistration;

import java.time.Instant;
import java.util.Optional;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface PendingRegistrationRepository {

  Optional<PendingRegistration> findByEmail(String email);

  Optional<PendingRegistration> findByRegistrationId(String registrationId);

  void deleteByExpiresAtBefore(Instant now);

  boolean existsByEmail(String email);
  
  PendingRegistration save(PendingRegistration entity);
  
  List<PendingRegistration> saveAll(Iterable<PendingRegistration> entities);
  
  Optional<PendingRegistration> findById(String id);
  
  boolean existsById(String id);
  
  List<PendingRegistration> findAll();
  
  List<PendingRegistration> findAllById(Iterable<String> ids);
  
  long count();
  
  void deleteById(String id);
  
  void delete(PendingRegistration entity);
  
  void deleteAll(Iterable<PendingRegistration> entities);
  
  PendingRegistration saveAndFlush(PendingRegistration entity);
  
  void flush();
  
  Page<PendingRegistration> findAll(Pageable pageable);
}

