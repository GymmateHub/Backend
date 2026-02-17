package com.gymmate.organisation.application;

import com.gymmate.organisation.domain.Organisation;
import com.gymmate.organisation.infrastructure.OrganisationRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.subscription.application.SubscriptionService;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Service for managing organisations (multi-tenant entities).
 * Provides business logic for organisation CRUD operations and lifecycle
 * management.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrganisationService {

    private final OrganisationRepository organisationRepository;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;

    /**
     * Orchestrates the creation of a new "Hub" (Organisation + Subscription + Owner
     * Link).
     * This is the main entry point for an authenticated OWNER to create their
     * organisation.
     */
    @Transactional
    public Organisation createHub(String name, String contactEmail, User owner) {
        String slug = generateSlug(name);

        // 1. Create Organisation
        Organisation organisation = createOrganisation(name, slug, contactEmail);

        // 2. Create Default Subscription (Starter/Trial)
        subscriptionService.createSubscription(organisation.getId(), "starter", true);

        // 3. Assign Owner and Link User
        assignOwner(organisation.getId(), owner.getId());

        // 4. Update User entity with organisationId
        owner.setOrganisationId(organisation.getId());
        userRepository.save(owner);

        return organisation;
    }

    /**
     * Create a new organisation without an owner (owner will be set later).
     * Internal method used by createHub.
     */
    @Transactional
    public Organisation createOrganisation(String name, String slug, String contactEmail) {
        log.debug("Creating organisation: {} with slug: {}", name, slug);

        // Validate slug uniqueness initially (optimization)
        if (organisationRepository.existsBySlug(slug)) {
            throw new DomainException("SLUG_EXISTS", "Organisation slug already exists: " + slug);
        }

        return createOrganisationWithRetry(name, slug, contactEmail, 0);
    }

    private Organisation createOrganisationWithRetry(String name, String slug, String contactEmail, int retryCount) {
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

        try {
            Organisation saved = organisationRepository.save(organisation);
            log.info("Organisation created successfully: {} (ID: {})", saved.getName(), saved.getId());
            return saved;
        } catch (DataIntegrityViolationException e) {
            if (retryCount >= 3) {
                log.error("Failed to create organisation after retries for slug: {}", slug, e);
                throw new DomainException("CREATION_FAILED",
                        "Failed to create organisation likely due to slug collision");
            }

            String newSlug = slug + "-" + UUID.randomUUID().toString().substring(0, 4);
            log.warn("Slug collision for {}, retrying with: {}", slug, newSlug);
            return createOrganisationWithRetry(name, newSlug, contactEmail, retryCount + 1);
        }
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
     * Find organisation by ID (returns Optional).
     */
    public Optional<Organisation> findById(UUID id) {
        return organisationRepository.findById(id);
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
     * Find organisation by owner user ID (returns Optional).
     */
    public Optional<Organisation> findByOwnerUserId(UUID ownerUserId) {
        return organisationRepository.findByOwnerUserId(ownerUserId);
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
        organisation.updateDetails(name, contactEmail, contactPhone, billingEmail, settings);

        log.info("Updated organisation details for: {}", organisationId);
        return organisationRepository.save(organisation);
    }

    /**
     * Update organisation limits (admin only).
     */
    @Transactional
    public Organisation updateLimits(UUID organisationId, Integer maxGyms, Integer maxMembers, Integer maxStaff) {
        Organisation organisation = getById(organisationId);
        organisation.updateLimits(maxGyms, maxMembers, maxStaff);

        log.info("Updated organisation limits for: {} - maxGyms={}, maxMembers={}, maxStaff={}",
                organisationId, organisation.getMaxGyms(), organisation.getMaxMembers(), organisation.getMaxStaff());
        return organisationRepository.save(organisation);
    }

    /**
     * Check if user belongs to organisation (is owner).
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

    /**
     * Get all active organisations.
     */
    public List<Organisation> getAllActive() {
        return organisationRepository.findAllActive();
    }

    /**
     * Get organisations by subscription status.
     */
    public List<Organisation> getBySubscriptionStatus(String status) {
        return organisationRepository.findBySubscriptionStatus(status);
    }

    /**
     * Get organisations with trials ending before a specific date.
     */
    public List<Organisation> getTrialsEndingBefore(LocalDateTime date) {
        return organisationRepository.findTrialsEndingBefore(date);
    }

    /**
     * Get organisations with subscriptions expiring between dates.
     */
    public List<Organisation> getSubscriptionsExpiringBetween(LocalDateTime start, LocalDateTime end) {
        return organisationRepository.findSubscriptionsExpiringBetween(start, end);
    }

    /**
     * Count active organisations.
     */
    public long countActive() {
        return organisationRepository.countActive();
    }

    /**
     * Check if slug exists.
     */
    public boolean slugExists(String slug) {
        return organisationRepository.existsBySlug(slug);
    }
}
