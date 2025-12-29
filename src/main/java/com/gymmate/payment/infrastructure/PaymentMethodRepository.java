package com.gymmate.payment.infrastructure;

import com.gymmate.payment.domain.PaymentMethod;
import com.gymmate.payment.domain.PaymentMethodOwnerType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for unified payment methods.
 * Supports both gym (platform) and member payment methods.
 */
@Repository
public interface PaymentMethodRepository extends JpaRepository<PaymentMethod, UUID> {

    // ============================================
    // Generic queries
    // ============================================

    Optional<PaymentMethod> findByProviderPaymentMethodId(String providerPaymentMethodId);

    List<PaymentMethod> findByOwnerTypeAndOwnerIdOrderByIsDefaultDescCreatedAtDesc(
            PaymentMethodOwnerType ownerType, UUID ownerId);

    Optional<PaymentMethod> findByOwnerTypeAndOwnerIdAndIsDefaultTrue(
            PaymentMethodOwnerType ownerType, UUID ownerId);

    @Modifying
    @Query("UPDATE PaymentMethod p SET p.isDefault = false WHERE p.ownerType = :ownerType AND p.ownerId = :ownerId")
    void clearDefaultForOwner(PaymentMethodOwnerType ownerType, UUID ownerId);

    boolean existsByOwnerTypeAndOwnerId(PaymentMethodOwnerType ownerType, UUID ownerId);

    void deleteByOwnerTypeAndOwnerIdAndId(PaymentMethodOwnerType ownerType, UUID ownerId, UUID id);

    // ============================================
    // Gym-specific queries (Platform payments)
    // ============================================

    default List<PaymentMethod> findByGymForPlatform(UUID gymId) {
        return findByOwnerTypeAndOwnerIdOrderByIsDefaultDescCreatedAtDesc(PaymentMethodOwnerType.GYM, gymId);
    }

    default Optional<PaymentMethod> findDefaultForGym(UUID gymId) {
        return findByOwnerTypeAndOwnerIdAndIsDefaultTrue(PaymentMethodOwnerType.GYM, gymId);
    }

    @Modifying
    default void clearDefaultForGym(UUID gymId) {
        clearDefaultForOwner(PaymentMethodOwnerType.GYM, gymId);
    }

    default boolean existsForGym(UUID gymId) {
        return existsByOwnerTypeAndOwnerId(PaymentMethodOwnerType.GYM, gymId);
    }

    // ============================================
    // Member-specific queries (Gym payments)
    // ============================================

    default List<PaymentMethod> findByMember(UUID memberId) {
        return findByOwnerTypeAndOwnerIdOrderByIsDefaultDescCreatedAtDesc(PaymentMethodOwnerType.MEMBER, memberId);
    }

    default Optional<PaymentMethod> findDefaultForMember(UUID memberId) {
        return findByOwnerTypeAndOwnerIdAndIsDefaultTrue(PaymentMethodOwnerType.MEMBER, memberId);
    }

    @Modifying
    default void clearDefaultForMember(UUID memberId) {
        clearDefaultForOwner(PaymentMethodOwnerType.MEMBER, memberId);
    }

    default boolean existsForMember(UUID memberId) {
        return existsByOwnerTypeAndOwnerId(PaymentMethodOwnerType.MEMBER, memberId);
    }

    // Find member payment methods by gym (for gym owner to view)
    @Query("SELECT p FROM PaymentMethod p WHERE p.ownerType = 'MEMBER' AND p.gymId = :gymId ORDER BY p.createdAt DESC")
    List<PaymentMethod> findMemberPaymentMethodsByGym(UUID gymId);

    // ============================================
    // Analytics queries
    // ============================================

    @Query("SELECT COUNT(p) FROM PaymentMethod p WHERE p.ownerType = :ownerType AND p.active = true")
    long countActiveByOwnerType(PaymentMethodOwnerType ownerType);

    @Query("SELECT COUNT(p) FROM PaymentMethod p WHERE p.gymId = :gymId AND p.active = true")
    long countActiveByGym(UUID gymId);

    @Query("SELECT p.methodType, COUNT(p) FROM PaymentMethod p WHERE p.active = true GROUP BY p.methodType")
    List<Object[]> countByMethodType();

    @Query("SELECT p.cardBrand, COUNT(p) FROM PaymentMethod p WHERE p.methodType = 'CARD' AND p.active = true GROUP BY p.cardBrand")
    List<Object[]> countByCardBrand();

    // ============================================
    // Backward compatibility aliases
    // ============================================

    default Optional<PaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId) {
        return findByProviderPaymentMethodId(stripePaymentMethodId);
    }

    // Legacy method for GymPaymentMethodRepository compatibility
    default List<PaymentMethod> findByGymIdOrderByIsDefaultDescCreatedAtDesc(UUID gymId) {
        return findByGymForPlatform(gymId);
    }

    default Optional<PaymentMethod> findByGymIdAndIsDefaultTrue(UUID gymId) {
        return findDefaultForGym(gymId);
    }

    default boolean existsByGymId(UUID gymId) {
        return existsForGym(gymId);
    }
}

