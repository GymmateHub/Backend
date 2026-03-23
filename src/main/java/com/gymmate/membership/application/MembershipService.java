package com.gymmate.membership.application;

import com.gymmate.membership.domain.*;
import com.gymmate.membership.infrastructure.FreezePolicyRepository;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.membership.infrastructure.MembershipPlanRepository;
import com.gymmate.notification.events.MembershipExpiredEvent;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

/**
 * Service for managing member memberships.
 * Implements FR-006 (Membership Lifecycle), FR-007 (Automated Billing).
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class MembershipService {

  private final MemberMembershipRepository membershipRepository;
  private final MembershipPlanRepository planRepository;
  private final FreezePolicyRepository freezePolicyRepository;
  private final MemberPaymentService memberPaymentService;
  private final ApplicationEventPublisher eventPublisher;

  /**
   * Subscribe a member to a membership plan.
   * FR-006: Activation workflow
   */
  public MemberMembership subscribeMember(UUID gymId, UUID memberId, UUID planId,
                                           LocalDate startDate) {
    log.info("Subscribing member {} to plan {} at gym {}", memberId, planId, gymId);

    // Validate no active membership exists
    membershipRepository.findActiveMembershipByMemberId(memberId)
      .ifPresent(existing -> {
        throw new DomainException("ACTIVE_MEMBERSHIP_EXISTS",
          "Member already has an active membership");
      });

    // Get plan details
    MembershipPlan plan = planRepository.findById(planId)
      .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", "id", planId.toString()));

    if (!plan.isActive()) {
      throw new DomainException("PLAN_NOT_ACTIVE", "Cannot subscribe to inactive plan");
    }

    // Calculate end date based on billing cycle
    LocalDate endDate = calculateEndDate(startDate, plan.getBillingCycle(), plan.getDurationMonths());
    LocalDate nextBillingDate = calculateNextBillingDate(startDate, plan.getBillingCycle());

    // Create membership
    MemberMembership membership = MemberMembership.builder()
      .memberId(memberId)
      .membershipPlanId(planId)
      .startDate(startDate)
      .endDate(endDate)
      .monthlyAmount(plan.getPrice())
      .billingCycle(plan.getBillingCycle())
      .nextBillingDate(nextBillingDate)
      .classCreditsRemaining(plan.getClassCredits())
      .guestPassesRemaining(plan.getGuestPasses())
      .trainerSessionsRemaining(plan.getTrainerSessions())
      .status(MembershipStatus.ACTIVE)
      .autoRenew(true)
      .frozen(false)
      .build();

    return membershipRepository.save(membership);
  }

  /**
   * Get active membership for a member.
   */
  @Transactional(readOnly = true)
  public MemberMembership getActiveMembership(UUID memberId) {
    return membershipRepository.findActiveMembershipByMemberId(memberId)
      .orElseThrow(() -> new ResourceNotFoundException("MemberMembership (active)", "memberId", memberId.toString()));
  }

  /**
   * Get membership by ID.
   */
  @Transactional(readOnly = true)
  public MemberMembership getMembershipById(UUID membershipId) {
    return membershipRepository.findById(membershipId)
      .orElseThrow(() -> new ResourceNotFoundException("MemberMembership", "id", membershipId.toString()));
  }

  /**
   * Get all memberships for a member (history).
   */
  @Transactional(readOnly = true)
  public List<MemberMembership> getMembershipHistory(UUID memberId) {
    return membershipRepository.findByMemberId(memberId);
  }

  /**
   * Get all memberships for a gym.
   */
  @Transactional(readOnly = true)
  public List<MemberMembership> getGymMemberships(UUID gymId) {
    return membershipRepository.findByGymId(gymId);
  }

  /**
   * Get memberships by status for a gym.
   */
  @Transactional(readOnly = true)
  public List<MemberMembership> getMembershipsByStatus(UUID gymId, MembershipStatus status) {
    return membershipRepository.findByGymIdAndStatus(gymId, status);
  }

  /**
   * Get memberships expiring soon (for renewal reminders).
   */
  @Transactional(readOnly = true)
  public List<MemberMembership> getExpiringMemberships(UUID gymId, int daysAhead) {
    LocalDateTime now = LocalDateTime.now();
    LocalDateTime futureDate = now.plusDays(daysAhead);
    return membershipRepository.findExpiringMemberships(gymId, now, futureDate);
  }

  /**
   * Freeze/hold a membership.
   * FR-006: Freeze/hold capabilities
   */
  public MemberMembership freezeMembership(UUID membershipId, LocalDate freezeUntil, String reason) {
    MemberMembership membership = getMembershipById(membershipId);

    if (membership.getStatus() != MembershipStatus.ACTIVE) {
      throw new DomainException("CANNOT_FREEZE", "Only active memberships can be frozen");
    }

    // Get freeze policy for gym (or default)
    FreezePolicy policy = freezePolicyRepository.findActiveByGymId(membership.getGymId())
      .or(() -> freezePolicyRepository.findDefaultPolicyByOrganisation(membership.getOrganisationId()))
      .or(() -> freezePolicyRepository.findDefaultPolicy())
      .orElse(createDefaultFreezePolicy());

    // Validate against policy
    if (!policy.canFreeze(membership)) {
      int daysSinceStart = (int) ChronoUnit.DAYS.between(membership.getStartDate(), LocalDate.now());
      if (daysSinceStart < policy.getMinMembershipDaysBeforeFreeze()) {
        throw new DomainException("FREEZE_TOO_EARLY",
          String.format("Membership must be active for at least %d days before freezing. Current: %d days",
            policy.getMinMembershipDaysBeforeFreeze(), daysSinceStart));
      }

      int remainingDays = policy.getRemainingFreezeDays(membership);
      if (remainingDays <= 0) {
        throw new DomainException("FREEZE_LIMIT_EXCEEDED",
          String.format("Annual freeze limit exceeded. Maximum: %d days, Used: %d days",
            policy.getMaxFreezeDaysPerYear(), membership.getTotalDaysFrozen()));
      }
    }

    // Validate freeze duration
    int requestedDays = (int) ChronoUnit.DAYS.between(LocalDate.now(), freezeUntil);
    if (!policy.isFreezeDurationValid(requestedDays)) {
      throw new DomainException("FREEZE_DURATION_INVALID",
        String.format("Freeze duration exceeds maximum. Maximum: %d days, Requested: %d days",
          policy.getMaxConsecutiveFreezeDays(), requestedDays));
    }

    // Check remaining freeze days
    int remainingDays = policy.getRemainingFreezeDays(membership);
    if (requestedDays > remainingDays) {
      throw new DomainException("INSUFFICIENT_FREEZE_DAYS",
        String.format("Requested freeze duration exceeds remaining days. Remaining: %d days, Requested: %d days",
          remainingDays, requestedDays));
    }

    membership.freeze(freezeUntil, reason);
    MemberMembership savedMembership = membershipRepository.save(membership);

    // Pause Stripe subscription if exists
    try {
      memberPaymentService.pauseMemberSubscription(membershipId);
    } catch (Exception e) {
      log.error("Failed to pause Stripe subscription for membership {}: {}", membershipId, e.getMessage());
      // Continue - membership is still frozen even if Stripe pause fails
    }

    log.info("Froze membership {} until {} (policy: {})", membershipId, freezeUntil, policy.getPolicyName());
    return savedMembership;
  }

  /**
   * Create a default freeze policy if none exists.
   */
  private FreezePolicy createDefaultFreezePolicy() {
    return FreezePolicy.builder()
      .policyName("Default Policy")
      .maxFreezeDaysPerYear(90)
      .maxConsecutiveFreezeDays(60)
      .minMembershipDaysBeforeFreeze(30)
      .coolingOffPeriodDays(30)
      .freezeFeeAmount(0.0)
      .freezeFeeFrequency("NONE")
      .allowPartialMonthFreeze(true)
      .isDefaultPolicy(true)

      .build();
  }

  /**
   * Unfreeze a membership.
   */
  public MemberMembership unfreezeMembership(UUID membershipId) {
    MemberMembership membership = getMembershipById(membershipId);

    if (!membership.isFrozen()) {
      throw new DomainException("NOT_FROZEN", "Membership is not frozen");
    }

    membership.unfreeze();
    MemberMembership savedMembership = membershipRepository.save(membership);

    // Resume Stripe subscription if exists
    try {
      memberPaymentService.resumeMemberSubscription(membershipId);
    } catch (Exception e) {
      log.error("Failed to resume Stripe subscription for membership {}: {}", membershipId, e.getMessage());
      // Continue - membership is still unfrozen even if Stripe resume fails
    }

    log.info("Unfroze membership {}", membershipId);
    return savedMembership;
  }

  /**
   * Renew a membership.
   * FR-006: Renewal workflows
   * FR-007: Automated billing
   */
  public MemberMembership renewMembership(UUID membershipId) {
    MemberMembership membership = getMembershipById(membershipId);

    if (!membership.isAutoRenew()) {
      throw new DomainException("AUTO_RENEW_DISABLED", "Auto-renewal is disabled for this membership");
    }

    // Get plan for renewal
    MembershipPlan plan = planRepository.findById(membership.getMembershipPlanId())
      .orElseThrow(() -> new ResourceNotFoundException("MembershipPlan", "id", membership.getMembershipPlanId().toString()));

    // Calculate new dates
    LocalDate newStartDate = membership.getEndDate().plusDays(1);
    LocalDate newEndDate = calculateEndDate(newStartDate, plan.getBillingCycle(), plan.getDurationMonths());
    LocalDate nextBillingDate = calculateNextBillingDate(newStartDate, plan.getBillingCycle());

    // Update membership
    membership.setStartDate(newStartDate);
    membership.setEndDate(newEndDate);
    membership.setNextBillingDate(nextBillingDate);
    membership.setStatus(MembershipStatus.ACTIVE);

    // Reset credits based on plan
    membership.setClassCreditsRemaining(plan.getClassCredits());
    membership.setGuestPassesRemaining(plan.getGuestPasses());
    membership.setTrainerSessionsRemaining(plan.getTrainerSessions());

    log.info("Renewed membership {} until {}", membershipId, newEndDate);
    return membershipRepository.save(membership);
  }

  /**
   * Cancel a membership.
   * FR-006: Cancellation workflows
   */
  public MemberMembership cancelMembership(UUID membershipId, boolean immediate) {
    MemberMembership membership = getMembershipById(membershipId);

    if (membership.getStatus() == MembershipStatus.CANCELLED) {
      throw new DomainException("ALREADY_CANCELLED", "Membership is already cancelled");
    }

    if (immediate) {
      membership.cancel();
      membership.setEndDate(LocalDate.now());
    } else {
      // Cancel at end of billing period
      membership.setAutoRenew(false);
      log.info("Membership {} will not auto-renew after {}", membershipId, membership.getEndDate());
    }

    log.info("Cancelled membership {}, immediate: {}", membershipId, immediate);
    return membershipRepository.save(membership);
  }

  /**
   * Use a class credit from the membership.
   */
  public MemberMembership useClassCredit(UUID membershipId) {
    MemberMembership membership = getMembershipById(membershipId);

    if (!membership.isActive()) {
      throw new DomainException("MEMBERSHIP_NOT_ACTIVE", "Membership is not active");
    }

    if (!membership.hasClassCreditsRemaining()) {
      throw new DomainException("NO_CREDITS", "No class credits remaining");
    }

    membership.useClassCredit();
    return membershipRepository.save(membership);
  }

  /**
   * Get count of active memberships for a gym.
   */
  @Transactional(readOnly = true)
  public long getActiveMembershipCount(UUID gymId) {
    return membershipRepository.countActiveByGymId(gymId);
  }

  /**
   * Process expired freezes - automatically unfreeze memberships past their freeze end date.
   * Called by scheduled task.
   */
  public int processExpiredFreezes() {
    LocalDate today = LocalDate.now();
    List<MemberMembership> frozenMemberships = membershipRepository.findFrozenMembershipsToUnfreeze(today);

    int unfrozenCount = 0;
    for (MemberMembership membership : frozenMemberships) {
      try {
        membership.unfreeze();
        membershipRepository.save(membership);

        // Resume Stripe subscription if exists
        try {
          memberPaymentService.resumeMemberSubscription(membership.getId());
        } catch (Exception e) {
          log.error("Failed to resume Stripe subscription for auto-unfrozen membership {}: {}",
            membership.getId(), e.getMessage());
        }

        log.info("Auto-unfroze membership {} (frozen until: {})", membership.getId(), membership.getFrozenUntil());
        unfrozenCount++;
      } catch (Exception e) {
        log.error("Error auto-unfreezing membership {}: {}", membership.getId(), e.getMessage());
      }
    }

    if (unfrozenCount > 0) {
      log.info("Auto-unfroze {} memberships", unfrozenCount);
    }

    return unfrozenCount;
  }

  /**
   * Process expired memberships — mark active memberships past their end date as EXPIRED.
   * Called by scheduled task daily.
   */
  public int processExpiredMemberships() {
    LocalDateTime now = LocalDateTime.now();
    List<MemberMembership> expired = membershipRepository.findExpiredActiveMemberships(now);

    int expiredCount = 0;
    for (MemberMembership membership : expired) {
      try {
        membership.expire();
        membershipRepository.save(membership);

        // Publish event for notification
        eventPublisher.publishEvent(MembershipExpiredEvent.builder()
                .organisationId(membership.getOrganisationId())
                .gymId(membership.getGymId())
                .memberId(membership.getMemberId())
                .membershipId(membership.getId())
                .expiredOn(membership.getEndDate())
                .build());

        log.info("Expired membership {} (end date: {})", membership.getId(), membership.getEndDate());
        expiredCount++;
      } catch (Exception e) {
        log.error("Error expiring membership {}: {}", membership.getId(), e.getMessage());
      }
    }

    if (expiredCount > 0) {
      log.info("Expired {} memberships", expiredCount);
    }
    return expiredCount;
  }

  /**
   * Process auto-renewals — attempt to renew active memberships past their end date
   * that have auto-renew enabled. Called by scheduled task daily.
   */
  public int processAutoRenewals() {
    LocalDateTime now = LocalDateTime.now();
    List<MemberMembership> toRenew = membershipRepository.findAutoRenewExpiredMemberships(now);

    int renewedCount = 0;
    for (MemberMembership membership : toRenew) {
      try {
        renewMembership(membership.getId());
        log.info("Auto-renewed membership {}", membership.getId());
        renewedCount++;
      } catch (Exception e) {
        log.error("Failed to auto-renew membership {}: {}", membership.getId(), e.getMessage());
        // If renewal fails, expire the membership
        try {
          membership.expire();
          membershipRepository.save(membership);
          log.warn("Marked membership {} as expired after failed auto-renewal", membership.getId());
        } catch (Exception ex) {
          log.error("Error expiring membership {} after failed renewal: {}", membership.getId(), ex.getMessage());
        }
      }
    }

    if (renewedCount > 0) {
      log.info("Auto-renewed {} memberships", renewedCount);
    }
    return renewedCount;
  }

  /**
   * Helper: Calculate end date based on billing cycle.
   */
  private LocalDate calculateEndDate(LocalDate startDate, String billingCycle, Integer durationMonths) {
    if ("lifetime".equalsIgnoreCase(billingCycle)) {
      return LocalDate.of(9999, 12, 31); // Far future date
    }

    if (durationMonths != null) {
      return startDate.plusMonths(durationMonths);
    }

    return switch (billingCycle.toLowerCase()) {
      case "monthly" -> startDate.plusMonths(1);
      case "quarterly" -> startDate.plusMonths(3);
      case "yearly", "annual" -> startDate.plusYears(1);
      default -> startDate.plusMonths(1);
    };
  }

  /**
   * Helper: Calculate next billing date based on billing cycle.
   */
  private LocalDate calculateNextBillingDate(LocalDate startDate, String billingCycle) {
    if ("lifetime".equalsIgnoreCase(billingCycle)) {
      return null; // No recurring billing
    }

    return switch (billingCycle.toLowerCase()) {
      case "monthly" -> startDate.plusMonths(1);
      case "quarterly" -> startDate.plusMonths(3);
      case "yearly", "annual" -> startDate.plusYears(1);
      default -> startDate.plusMonths(1);
    };
  }
}

