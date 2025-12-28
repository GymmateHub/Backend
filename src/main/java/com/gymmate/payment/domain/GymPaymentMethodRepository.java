package com.gymmate.payment.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GymPaymentMethodRepository extends JpaRepository<GymPaymentMethod, UUID> {

    List<GymPaymentMethod> findByGymIdOrderByIsDefaultDescCreatedAtDesc(UUID gymId);

    Optional<GymPaymentMethod> findByGymIdAndIsDefaultTrue(UUID gymId);

    Optional<GymPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    @Modifying
    @Query("UPDATE GymPaymentMethod p SET p.isDefault = false WHERE p.gymId = :gymId")
    void clearDefaultForGym(UUID gymId);

    boolean existsByGymId(UUID gymId);

    void deleteByGymIdAndId(UUID gymId, UUID id);
}

