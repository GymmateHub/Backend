package com.gymmate.membership.internal.application.port;

import com.gymmate.membership.internal.domain.MemberPaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface MemberPaymentMethodRepository {

    List<MemberPaymentMethod> findByMemberIdAndGymIdOrderByIsDefaultDescCreatedAtDesc(UUID memberId, UUID gymId);

    Optional<MemberPaymentMethod> findByMemberIdAndGymIdAndIsDefaultTrue(UUID memberId, UUID gymId);

    Optional<MemberPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    void clearDefaultForMember(UUID memberId, UUID gymId);

    boolean existsByMemberIdAndGymId(UUID memberId, UUID gymId);

MemberPaymentMethod save(MemberPaymentMethod entity);

List<MemberPaymentMethod> saveAll(Iterable<MemberPaymentMethod> entities);

Optional<MemberPaymentMethod> findById(UUID id);

boolean existsById(UUID id);

List<MemberPaymentMethod> findAll();

List<MemberPaymentMethod> findAllById(Iterable<UUID> ids);

long count();

void deleteById(UUID id);

void delete(MemberPaymentMethod entity);

void deleteAll(Iterable<MemberPaymentMethod> entities);

MemberPaymentMethod saveAndFlush(MemberPaymentMethod entity);

void flush();

Page<MemberPaymentMethod> findAll(Pageable pageable);
}

