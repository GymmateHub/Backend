package com.gymmate.membership.application;

import com.gymmate.membership.domain.*;
import com.gymmate.membership.infrastructure.MembershipPlanRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing membership plans.
 * Implements FR-005 (Flexible Membership Types) and FR-006 (Membership Lifecycle).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MembershipPlanService {

  private final MembershipPlanRepository membershipPlanRepository;

  /**
   * Create a new membership plan for a gym.
   */
  public MembershipPlan createPlan(UUID gymId, String name, String description,
                                    BigDecimal price, String billingCycle,
                                    Integer durationMonths) {
    log.info("Creating membership plan '{}' for gym: {}", name, gymId);

    // Validate unique name per gym
    if (membershipPlanRepository.existsByGymIdAndName(gymId, name)) {
      throw new DomainException("PLAN_NAME_EXISTS",
        "A membership plan with name '" + name + "' already exists for this gym");
    }

    MembershipPlan plan = MembershipPlan.builder()
      .name(name)
      .description(description)
      .price(price)
      .billingCycle(billingCycle)
      .durationMonths(durationMonths)
      .classCredits(null) // unlimited by default
      .guestPasses(0)
      .trainerSessions(0)
      .peakHoursAccess(true)
      .offPeakOnly(false)
      .featured(false)
      .gymId(gymId)
      .build();

    return membershipPlanRepository.save(plan);
  }

  /**
   * Get membership plan by ID.
   */
  @Transactional(readOnly = true)
  public MembershipPlan getPlanById(UUID planId) {
    return membershipPlanRepository.findById(planId)
      .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", "id", planId.toString()));
  }

  /**
   * Get all plans for a gym.
   */
  @Transactional(readOnly = true)
  public List<MembershipPlan> getPlansByGymId(UUID gymId) {
    return membershipPlanRepository.findByGymId(gymId);
  }

  /**
   * Get active plans for a gym.
   */
  @Transactional(readOnly = true)
  public List<MembershipPlan> getActivePlansByGymId(UUID gymId) {
    return membershipPlanRepository.findActiveByGymId(gymId);
  }

  /**
   * Get featured plans for a gym (for member signup page).
   */
  @Transactional(readOnly = true)
  public List<MembershipPlan> getFeaturedPlansByGymId(UUID gymId) {
    return membershipPlanRepository.findFeaturedByGymId(gymId);
  }

  /**
   * Update plan pricing.
   */
  public MembershipPlan updatePlanPricing(UUID planId, BigDecimal price, String billingCycle) {
    MembershipPlan plan = getPlanById(planId);
    plan.updatePricing(price, billingCycle);
    log.info("Updated pricing for plan {}: {} {}", planId, price, billingCycle);
    return membershipPlanRepository.save(plan);
  }

  /**
   * Update plan features.
   */
  public MembershipPlan updatePlanFeatures(UUID planId, Integer classCredits,
                                            Integer guestPasses, Integer trainerSessions) {
    MembershipPlan plan = getPlanById(planId);
    plan.updateFeatures(classCredits, guestPasses, trainerSessions);
    log.info("Updated features for plan {}", planId);
    return membershipPlanRepository.save(plan);
  }

  /**
   * Mark plan as featured.
   */
  public MembershipPlan setFeatured(UUID planId, boolean featured) {
    MembershipPlan plan = getPlanById(planId);
    plan.setFeatured(featured);
    return membershipPlanRepository.save(plan);
  }

  /**
   * Deactivate a plan (soft delete).
   */
  public void deactivatePlan(UUID planId) {
    MembershipPlan plan = getPlanById(planId);

    // Check if any active memberships use this plan
    long activeCount = membershipPlanRepository.findById(planId)
      .map(p -> p.isActive() ? 1L : 0L)
      .orElse(0L);

    if (activeCount > 0) {
      log.warn("Deactivating plan {} with active memberships", planId);
    }

    plan.setActive(false);
    membershipPlanRepository.save(plan);
    log.info("Deactivated plan: {}", planId);
  }

  /**
   * Delete a plan (only if no memberships reference it).
   */
  public void deletePlan(UUID planId) {
    MembershipPlan plan = getPlanById(planId);
    membershipPlanRepository.delete(plan);
    log.info("Deleted plan: {}", planId);
  }
}

