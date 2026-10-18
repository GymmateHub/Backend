package com.gymmate.shared.application.port;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * Generic CRUD contract of an aggregate repository port. Module ports extend it and declare
 * only their own finders, e.g. {@code interface LeadRepository extends DomainRepository<Lead, UUID>}.
 *
 * <p>Deliberately has no {@code flush}/{@code saveAndFlush}: flushing is a persistence concern
 * the application layer never needs (changes to loaded aggregates are flushed at commit).
 *
 * @param <D>  domain aggregate type
 * @param <ID> identifier type
 */
public interface DomainRepository<D, ID> {

    D save(D aggregate);

    List<D> saveAll(Iterable<D> aggregates);

    Optional<D> findById(ID id);

    boolean existsById(ID id);

    List<D> findAll();

    Page<D> findAll(Pageable pageable);

    List<D> findAllById(Iterable<ID> ids);

    long count();

    void deleteById(ID id);

    void delete(D aggregate);
}
