package com.gymmate.membership.application;

import com.gymmate.membership.domain.*;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.membership.infrastructure.MembershipPlanRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.exception.DomainException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    membership.setGymId(gymId);
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

    membership.freeze(freezeUntil, reason);
    log.info("Froze membership {} until {}", membershipId, freezeUntil);
    return membershipRepository.save(membership);
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
    log.info("Unfroze membership {}", membershipId);
    return membershipRepository.save(membership);
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

