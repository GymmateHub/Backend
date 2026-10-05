package com.gymmate.access.internal.application.port;

import com.gymmate.access.internal.domain.AccessLog;

import java.util.Optional;
import java.util.UUID;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface AccessLogRepository {

    Optional<AccessLog> findTopByMemberIdOrderByAccessTimeDesc(UUID memberId);

AccessLog save(AccessLog entity);

List<AccessLog> saveAll(Iterable<AccessLog> entities);

Optional<AccessLog> findById(UUID id);

boolean existsById(UUID id);

List<AccessLog> findAll();

List<AccessLog> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(AccessLog entity);

void deleteAll(Iterable<AccessLog> entities);

AccessLog saveAndFlush(AccessLog entity);

void flush();

Page<AccessLog> findAll(Pageable pageable);
}
