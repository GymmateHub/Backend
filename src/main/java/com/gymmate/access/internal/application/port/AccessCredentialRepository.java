package com.gymmate.access.internal.application.port;

import com.gymmate.access.internal.domain.AccessCredential;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface AccessCredentialRepository {

  Optional<AccessCredential> findByTokenHashAndActiveTrue(String tokenHash);

  List<AccessCredential> findByMemberId(UUID memberId);

  boolean existsByTokenHash(String tokenHash);
  
  AccessCredential save(AccessCredential entity);
  
  List<AccessCredential> saveAll(Iterable<AccessCredential> entities);
  
  Optional<AccessCredential> findById(UUID id);
  
  boolean existsById(UUID id);
  
  List<AccessCredential> findAll();
  
  List<AccessCredential> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void delete(AccessCredential entity);
  
  void deleteAll(Iterable<AccessCredential> entities);
  
  AccessCredential saveAndFlush(AccessCredential entity);
  
  void flush();
  
  Page<AccessCredential> findAll(Pageable pageable);
}
