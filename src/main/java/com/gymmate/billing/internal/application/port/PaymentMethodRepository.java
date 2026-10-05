package com.gymmate.billing.internal.application.port;

import com.gymmate.billing.internal.domain.PaymentMethod;
import com.gymmate.shared.constants.PaymentMethodOwnerType;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Repository for unified payment methods.
 * Supports both organisation (platform) and member payment methods.
 */
public interface PaymentMethodRepository {

    // ============================================
    // Generic queries
    // ============================================

    Optional<PaymentMethod> findByProviderPaymentMethodId(String providerPaymentMethodId);

    List<PaymentMethod> findByOwnerTypeAndOwnerIdOrderByIsDefaultDescCreatedAtDesc(
            PaymentMethodOwnerType ownerType, UUID ownerId);

    Optional<PaymentMethod> findByOwnerTypeAndOwnerIdAndIsDefaultTrue(
            PaymentMethodOwnerType ownerType, UUID ownerId);

    void clearDefaultForOwner(PaymentMethodOwnerType ownerType, UUID ownerId);

    boolean existsByOwnerTypeAndOwnerId(PaymentMethodOwnerType ownerType, UUID ownerId);

    void deleteByOwnerTypeAndOwnerIdAndId(PaymentMethodOwnerType ownerType, UUID ownerId, UUID id);

    // ============================================
    // Organisation-specific queries (preferred)
    // ============================================

    List<PaymentMethod> findByOrganisationId(UUID organisationId);

    Optional<PaymentMethod> findDefaultForOrganisation(UUID organisationId);

    void clearDefaultForOrganisation(UUID organisationId);

    boolean existsForOrganisation(UUID organisationId);

    // ============================================
    // Member-specific queries (Gym payments)
    // ============================================

    default List<PaymentMethod> findByMember(UUID memberId) {
        return findByOwnerTypeAndOwnerIdOrderByIsDefaultDescCreatedAtDesc(PaymentMethodOwnerType.MEMBER, memberId);
    }

    default Optional<PaymentMethod> findDefaultForMember(UUID memberId) {
        return findByOwnerTypeAndOwnerIdAndIsDefaultTrue(PaymentMethodOwnerType.MEMBER, memberId);
    }

    default void clearDefaultForMember(UUID memberId) {
        clearDefaultForOwner(PaymentMethodOwnerType.MEMBER, memberId);
    }

    default boolean existsForMember(UUID memberId) {
        return existsByOwnerTypeAndOwnerId(PaymentMethodOwnerType.MEMBER, memberId);
    }

    // Find member payment methods by gym (for gym owner to view)
    List<PaymentMethod> findMemberPaymentMethodsByGym(UUID gymId);

    // ============================================
    // Analytics queries
    // ============================================

    long countActiveByOwnerType(PaymentMethodOwnerType ownerType);

    long countActiveByGym(UUID gymId);

    List<Object[]> countByMethodType();

    List<Object[]> countByCardBrand();

    // ============================================
    // Backward compatibility aliases
    // ============================================

    default Optional<PaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId) {
        return findByProviderPaymentMethodId(stripePaymentMethodId);
    }
    
    PaymentMethod save(PaymentMethod entity);
    
    List<PaymentMethod> saveAll(Iterable<PaymentMethod> entities);
    
    Optional<PaymentMethod> findById(UUID id);
    
    boolean existsById(UUID id);
    
    List<PaymentMethod> findAll();
    
    List<PaymentMethod> findAllById(Iterable<UUID> ids);
    
    long count();
    
    void deleteById(UUID id);
    
    void delete(PaymentMethod entity);
    
    void deleteAll(Iterable<PaymentMethod> entities);
    
    PaymentMethod saveAndFlush(PaymentMethod entity);
    
    void flush();
    
    Page<PaymentMethod> findAll(Pageable pageable);
}
