package com.gymmate.user.application;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.Trainer;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.TrainerRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application service for trainer management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TrainerService {

    private final TrainerRepository trainerRepository;
    private final UserRepository userRepository;

    /**
     * Create a new trainer profile for an existing user.
     */
    @Transactional
    public Trainer createTrainer(UUID userId, String[] specializations, String bio,
                                 BigDecimal hourlyRate, BigDecimal commissionRate,
                                 LocalDate hireDate, String employmentType) {
        // Verify user exists
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId.toString()));

        // Check if trainer profile already exists
        if (trainerRepository.existsByUserId(userId)) {
            throw new DomainException("TRAINER_ALREADY_EXISTS",
                    "Trainer profile already exists for user: " + userId);
        }

        // Create trainer
        Trainer trainer = Trainer.builder()
                .userId(userId)
                .specializations(specializations)
                .bio(bio)
                .hourlyRate(hourlyRate)
                .commissionRate(commissionRate != null ? commissionRate : BigDecimal.ZERO)
                .hireDate(hireDate != null ? hireDate : LocalDate.now())
                .employmentType(employmentType)
                .acceptingClients(true)
                .certifications("[]")
                .build();

        // Set organisationId from linked user (TenantEntity.prePersist will also attempt via TenantContext)
        if (user.getOrganisationId() != null) {
            trainer.setOrganisationId(user.getOrganisationId());
        }

        return trainerRepository.save(trainer);
    }

    /**
     * Find trainer by ID.
     */
    public Trainer findById(UUID id) {
        return trainerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", id.toString()));
    }

    /**
     * Find trainer by user ID.
     */
    public Trainer findByUserId(UUID userId) {
        return trainerRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Trainer", "userId=" + userId));
    }

    /**
     * Update trainer rates.
     */
    @Transactional
    public Trainer updateRate(UUID trainerId, BigDecimal hourlyRate, BigDecimal commissionRate) {
        Trainer trainer = findById(trainerId);
        trainer.updateRate(hourlyRate, commissionRate);
        return trainerRepository.save(trainer);
    }

    /**
     * Update trainer availability.
     */
    @Transactional
    public Trainer updateAvailability(UUID trainerId, String availabilityJson) {
        Trainer trainer = findById(trainerId);
        trainer.updateAvailability(availabilityJson);
        return trainerRepository.save(trainer);
    }

    /**
     * Toggle accepting clients status.
     */
    @Transactional
    public Trainer toggleAcceptingClients(UUID trainerId) {
        Trainer trainer = findById(trainerId);
        trainer.toggleAcceptingClients();
        return trainerRepository.save(trainer);
    }

    /**
     * Update trainer bio.
     */
    @Transactional
    public Trainer updateBio(UUID trainerId, String bio) {
        Trainer trainer = findById(trainerId);
        trainer.setBio(bio);
        return trainerRepository.save(trainer);
    }

    /**
     * Update trainer specializations.
     */
    @Transactional
    public Trainer updateSpecializations(UUID trainerId, String[] specializations) {
        Trainer trainer = findById(trainerId);
        trainer.setSpecializations(specializations);
        return trainerRepository.save(trainer);
    }

    /**
     * Update trainer certifications.
     */
    @Transactional
    public Trainer updateCertifications(UUID trainerId, String certificationsJson) {
        Trainer trainer = findById(trainerId);
        trainer.setCertifications(certificationsJson);
        return trainerRepository.save(trainer);
    }

    /**
     * Find all trainers accepting clients within the current organisation.
     */
    public List<Trainer> findActiveAndAcceptingClients(UUID organisationId) {
        return trainerRepository.findActiveAndAcceptingClientsByOrganisationId(organisationId);
    }

    /**
     * Find trainers by employment type within the current organisation.
     */
    public List<Trainer> findByEmploymentType(UUID organisationId, String employmentType) {
        return trainerRepository.findByOrganisationIdAndEmploymentType(organisationId, employmentType);
    }

    /**
     * Find all trainers within the current organisation.
     */
    public List<Trainer> findAllByOrganisation(UUID organisationId) {
        return trainerRepository.findByOrganisationId(organisationId);
    }

    /**
     * @deprecated Use {@link #findAllByOrganisation(UUID)} instead.
     */
    @Deprecated
    public List<Trainer> findAll() {
        return trainerRepository.findAll();
    }

    /**
     * @deprecated Use {@link #findActiveAndAcceptingClients(UUID)} instead.
     */
    @Deprecated
    public List<Trainer> findActiveAndAcceptingClients() {
        return trainerRepository.findActiveAndAcceptingClients();
    }

    /**
     * Deactivate trainer.
     */
    @Transactional
    public Trainer deactivate(UUID trainerId) {
        Trainer trainer = findById(trainerId);
        trainer.setActive(false);
        trainer.setAcceptingClients(false);
        return trainerRepository.save(trainer);
    }

    /**
     * Activate trainer.
     */
    @Transactional
    public Trainer activate(UUID trainerId) {
        Trainer trainer = findById(trainerId);
        trainer.setActive(true);
        return trainerRepository.save(trainer);
    }
}
