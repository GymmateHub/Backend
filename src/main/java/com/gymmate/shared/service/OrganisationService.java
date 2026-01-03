package com.gymmate.shared.service;

import com.gymmate.shared.domain.Organisation;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.infrastructure.OrganisationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service for managing organisations (multi-tenant entities).
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganisationService {

    private final OrganisationRepository organisationRepository;

    /**
     * Create a new organisation without an owner (owner will be set later).
     * This is step 1 in the registration flow.
     */
    @Transactional
    public Organisation createOrganisation(String name, String slug, String contactEmail) {
        log.debug("Creating organisation: {} with slug: {}", name, slug);

        // Validate slug uniqueness
        if (organisationRepository.existsBySlug(slug)) {
            throw new DomainException("SLUG_EXISTS", "Organisation slug already exists: " + slug);
        }

        // Calculate trial end date (e.g., 14 days from now)
        LocalDateTime trialEndsAt = LocalDateTime.now().plusDays(14);

        Organisation organisation = Organisation.builder()
                .name(name)
                .slug(slug)
                .contactEmail(contactEmail)
                .subscriptionPlan("starter")
                .subscriptionStatus("trial")
                .trialEndsAt(trialEndsAt)
                .maxGyms(1)
                .maxMembers(200)
                .maxStaff(10)
                .onboardingCompleted(false)
                .build();

        // Note: isActive defaults to true via BaseAuditEntity
        Organisation saved = organisationRepository.save(organisation);
        log.info("Organisation created successfully: {} (ID: {})", saved.getName(), saved.getId());

        return saved;
    }

    /**
     * Assign an owner to an organisation.
     * This is step 3 in the registration flow (after user creation).
     */
    @Transactional
    public void assignOwner(UUID organisationId, UUID userId) {
        log.debug("Assigning owner {} to organisation {}", userId, organisationId);

        Organisation organisation = organisationRepository.findById(organisationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", organisationId.toString()));

        if (organisation.getOwnerUserId() != null) {
            throw new DomainException("OWNER_ALREADY_SET",
                "Organisation already has an owner assigned");
        }

        organisation.assignOwner(userId);
        organisationRepository.save(organisation);

        log.info("Owner {} assigned to organisation {}", userId, organisationId);
    }

    /**
     * Get organisation by ID.
     */
    public Organisation getById(UUID id) {
        return organisationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", id.toString()));
    }

    /**
     * Get organisation by slug.
     */
    public Organisation getBySlug(String slug) {
        return organisationRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", "slug", slug));
    }

    /**
     * Get organisation by owner user ID.
     */
    public Organisation getByOwnerUserId(UUID ownerUserId) {
        return organisationRepository.findByOwnerUserId(ownerUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Organisation", "owner", ownerUserId.toString()));
    }

    /**
     * Update organisation subscription.
     */
    @Transactional
    public Organisation updateSubscription(UUID organisationId, String plan, String status, LocalDateTime expiresAt) {
        Organisation organisation = getById(organisationId);
        organisation.updateSubscription(plan, status, expiresAt);
        return organisationRepository.save(organisation);
    }

    /**
     * Activate organisation.
     */
    @Transactional
    public Organisation activate(UUID organisationId) {
        Organisation organisation = getById(organisationId);
        organisation.activate();
        return organisationRepository.save(organisation);
    }

    /**
     * Deactivate organisation.
     */
    @Transactional
    public Organisation deactivate(UUID organisationId) {
        Organisation organisation = getById(organisationId);
        organisation.deactivate();
        return organisationRepository.save(organisation);
    }

    /**
     * Complete onboarding for organisation.
     */
    @Transactional
    public Organisation completeOnboarding(UUID organisationId) {
        Organisation organisation = getById(organisationId);
        organisation.completeOnboarding();
        return organisationRepository.save(organisation);
    }

    /**
     * Update organisation details.
     */
    @Transactional
    public Organisation updateDetails(UUID organisationId, String name, String contactEmail,
                                       String contactPhone, String billingEmail, String settings) {
        Organisation organisation = getById(organisationId);

        if (name != null && !name.isBlank()) {
            organisation.setName(name);
        }
        if (contactEmail != null && !contactEmail.isBlank()) {
            organisation.setContactEmail(contactEmail);
        }
        if (contactPhone != null) {
            organisation.setContactPhone(contactPhone);
        }
        if (billingEmail != null && !billingEmail.isBlank()) {
            organisation.setBillingEmail(billingEmail);
        }
        if (settings != null) {
            organisation.setSettings(settings);
        }

        log.info("Updated organisation details for: {}", organisationId);
        return organisationRepository.save(organisation);
    }

    /**
     * Update organisation limits (admin only).
     */
    @Transactional
    public Organisation updateLimits(UUID organisationId, Integer maxGyms, Integer maxMembers, Integer maxStaff) {
        Organisation organisation = getById(organisationId);

        if (maxGyms != null && maxGyms > 0) {
            organisation.setMaxGyms(maxGyms);
        }
        if (maxMembers != null && maxMembers > 0) {
            organisation.setMaxMembers(maxMembers);
        }
        if (maxStaff != null && maxStaff > 0) {
            organisation.setMaxStaff(maxStaff);
        }

        log.info("Updated organisation limits for: {} - maxGyms={}, maxMembers={}, maxStaff={}",
            organisationId, organisation.getMaxGyms(), organisation.getMaxMembers(), organisation.getMaxStaff());
        return organisationRepository.save(organisation);
    }

    /**
     * Check if user belongs to organisation.
     */
    public boolean userBelongsToOrganisation(UUID userId, UUID organisationId) {
        Organisation organisation = getById(organisationId);
        return organisation.getOwnerUserId() != null && organisation.getOwnerUserId().equals(userId);
    }

    /**
     * Generate a unique slug from organisation name.
     */
    public String generateSlug(String name) {
        String baseSlug = name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();

        String slug = baseSlug;
        int counter = 1;

        while (organisationRepository.existsBySlug(slug)) {
            slug = baseSlug + "-" + counter;
            counter++;
        }

        return slug;
    }
}

