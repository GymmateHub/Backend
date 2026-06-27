package com.gymmate.access.application;

import com.gymmate.access.application.port.AccessDevicePort;
import com.gymmate.access.domain.AccessCredential;
import com.gymmate.access.domain.AccessEvent;
import com.gymmate.access.domain.AccessPoint;
import com.gymmate.access.domain.enums.AccessDecision;
import com.gymmate.access.domain.enums.AccessDirection;
import com.gymmate.access.domain.enums.AccessPointMode;
import com.gymmate.access.domain.enums.CredentialType;
import com.gymmate.access.domain.enums.DenyReason;
import com.gymmate.access.events.AccessDeniedEvent;
import com.gymmate.access.events.TailgatingSuspectedEvent;
import com.gymmate.access.infrastructure.AccessCredentialRepository;
import com.gymmate.access.infrastructure.AccessEventRepository;
import com.gymmate.access.infrastructure.AccessPointRepository;
import com.gymmate.access.infrastructure.AccessScheduleRepository;
import com.gymmate.access.infrastructure.DoorBenefitRepository;
import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.infrastructure.MemberMembershipRepository;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.user.domain.Member;
import com.gymmate.user.infrastructure.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Access-control core. Enforces the entry-decision pipeline and the
 * anti-tailgating layer in software, persists every attempt as an
 * {@link AccessEvent}, and drives the physical layer through
 * {@link AccessDevicePort} adapters selected per access-point mode.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class AccessService {

  private static final SecureRandom RANDOM = new SecureRandom();

  private final AccessPointRepository accessPointRepository;
  private final AccessCredentialRepository accessCredentialRepository;
  private final AccessEventRepository accessEventRepository;
  private final DoorBenefitRepository doorBenefitRepository;
  private final AccessScheduleRepository accessScheduleRepository;
  private final MemberRepository memberRepository;
  private final MemberMembershipRepository memberMembershipRepository;
  private final List<AccessDevicePort> devicePorts;
  private final ApplicationEventPublisher eventPublisher;

  // ------------------------------------------------------------------
  // Scan / entry decision
  // ------------------------------------------------------------------

  /**
   * Validate a credential scan at an access point and decide entry. Records and
   * returns the resulting {@link AccessEvent} (granted or denied).
   */
  @Transactional
  public AccessEvent scan(String rawToken, UUID accessPointId, AccessDirection direction) {
    AccessPoint point = accessPointRepository.findById(accessPointId)
        .orElseThrow(() -> new ResourceNotFoundException("AccessPoint", accessPointId.toString()));

    AccessDirection dir = direction == null ? AccessDirection.IN : direction;

    AccessCredential credential = (rawToken == null) ? null
        : accessCredentialRepository.findByTokenHashAndActiveTrue(sha256(rawToken)).orElse(null);

    Member member = null;
    if (credential != null && !credential.isExpired()) {
      member = memberRepository.findById(credential.getMemberId()).orElse(null);
    }

    // Exit is always recorded (no entitlement checks) to keep occupancy accurate.
    if (dir == AccessDirection.OUT) {
      return record(point, credential, member, dir, AccessDecision.GRANTED, null, false, null);
    }

    if (credential == null || credential.isExpired() || member == null) {
      return deny(point, credential, member, dir, DenyReason.INVALID_CREDENTIAL);
    }

    DenyReason reason = evaluateEntitlement(point, member);
    if (reason != null) {
      return deny(point, credential, member, dir, reason);
    }

    String tailgating = evaluateTailgating(point, credential, member);
    if (tailgating != null) {
      AccessEvent ev = record(point, credential, member, dir,
          AccessDecision.DENIED, DenyReason.TAILGATING_BLOCKED, true, tailgating);
      eventPublisher.publishEvent(TailgatingSuspectedEvent.builder()
          .organisationId(point.getOrganisationId()).gymId(point.getGymId())
          .memberId(member.getId()).accessPointId(point.getId())
          .accessPointName(point.getName()).reason(tailgating).build());
      return ev;
    }

    devicePort(point).openOnce(point);
    return record(point, credential, member, dir, AccessDecision.GRANTED, null, false, null);
  }

  /** Entry-decision pipeline. Returns the first failing reason, or null if allowed. */
  private DenyReason evaluateEntitlement(AccessPoint point, Member member) {
    if (member.getStatus() == MemberStatus.SUSPENDED) {
      return DenyReason.SUSPENDED_OR_FROZEN;
    }
    if (!member.isActive()) {
      return DenyReason.NO_ACTIVE_MEMBERSHIP;
    }
    Optional<MemberMembership> membershipOpt =
        memberMembershipRepository.findActiveMembershipByMemberId(member.getId());
    if (membershipOpt.isEmpty()) {
      return DenyReason.NO_ACTIVE_MEMBERSHIP;
    }
    MemberMembership membership = membershipOpt.get();

    if (!member.isWaiverSigned()) {
      return DenyReason.INCOMPLETE_SIGNUP;
    }

    UUID planId = membership.getMembershipPlanId();

    // Door benefit: if any benefit is configured for this point, the member's
    // plan must be among them.
    if (doorBenefitRepository.existsByAccessPointId(point.getId())
        && (planId == null
            || !doorBenefitRepository.existsByAccessPointIdAndMembershipPlanId(point.getId(), planId))) {
      return DenyReason.NO_DOOR_BENEFIT;
    }

    // Access schedule: if windows exist for this plan, now must fall within one.
    if (planId != null) {
      var schedules = accessScheduleRepository.findByMembershipPlanId(planId);
      if (!schedules.isEmpty()) {
        LocalDateTime now = LocalDateTime.now();
        boolean withinWindow = schedules.stream()
            .anyMatch(s -> s.matches(now.getDayOfWeek(), now.toLocalTime()));
        if (!withinWindow) {
          return DenyReason.OUTSIDE_ACCESS_TIMES;
        }
      }
    }

    // OVERDUE_OVER_LIMIT, VISITS_EXHAUSTED, STOP_AT_GATE_TASK are reserved for
    // when balance/visit-pack/gate-task data is available.
    return null;
  }

  /** Anti-tailgating checks (run after a grant). Returns a reason string when blocked. */
  private String evaluateTailgating(AccessPoint point, AccessCredential credential, Member member) {
    // One-open-session / pass-back: member already inside with no exit recorded.
    Optional<AccessEvent> lastGranted = accessEventRepository
        .findTopByMemberIdAndDecisionOrderByOccurredAtDesc(member.getId(), AccessDecision.GRANTED);
    if (lastGranted.isPresent() && lastGranted.get().getDirection() == AccessDirection.IN) {
      return "member already inside (no exit recorded)";
    }

    // Re-entry lockout: credential re-used too soon to wave a second person in.
    int lockout = point.getReentryLockoutSeconds() == null ? 0 : point.getReentryLockoutSeconds();
    if (lockout > 0) {
      Optional<AccessEvent> lastIn = accessEventRepository
          .findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(
              credential.getId(), AccessDecision.GRANTED, AccessDirection.IN);
      if (lastIn.isPresent()
          && lastIn.get().getOccurredAt().isAfter(LocalDateTime.now().minusSeconds(lockout))) {
        return "credential re-used within " + lockout + "s lockout window";
      }
    }
    return null;
  }

  private AccessEvent deny(AccessPoint point, AccessCredential credential, Member member,
                           AccessDirection dir, DenyReason reason) {
    AccessEvent ev = record(point, credential, member, dir, AccessDecision.DENIED, reason, false, null);
    eventPublisher.publishEvent(AccessDeniedEvent.builder()
        .organisationId(point.getOrganisationId()).gymId(point.getGymId())
        .memberId(member != null ? member.getId() : null)
        .accessPointId(point.getId()).accessPointName(point.getName())
        .denyReason(reason).build());
    return ev;
  }

  private AccessEvent record(AccessPoint point, AccessCredential credential, Member member,
                             AccessDirection dir, AccessDecision decision, DenyReason reason,
                             boolean tailgating, String note) {
    AccessEvent ev = AccessEvent.builder()
        .accessPointId(point.getId())
        .credentialId(credential != null ? credential.getId() : null)
        .memberId(member != null ? member.getId() : null)
        .direction(dir)
        .decision(decision)
        .denyReason(reason)
        .tailgatingSuspected(tailgating)
        .occurredAt(LocalDateTime.now())
        .note(note)
        .build();
    ev.setGymId(point.getGymId());
    ev.setOrganisationId(point.getOrganisationId());
    return accessEventRepository.save(ev);
  }

  private AccessDevicePort devicePort(AccessPoint point) {
    AccessPointMode mode = point.getMode() == null ? AccessPointMode.SOFTWARE : point.getMode();
    return devicePorts.stream().filter(d -> d.supportedMode() == mode).findFirst()
        .orElseGet(() -> devicePorts.stream()
            .filter(d -> d.supportedMode() == AccessPointMode.SOFTWARE).findFirst()
            .orElseThrow(() -> new IllegalStateException("No access device adapter available")));
  }

  // ------------------------------------------------------------------
  // Device reconciliation (turnstile / CV)
  // ------------------------------------------------------------------

  /**
   * Reconcile a hardware device report against valid scans. If more people
   * passed than there were valid scans, flag tailgating, attach any captured
   * image, and raise a real-time alert.
   */
  @Transactional
  public AccessEvent handleDeviceEvent(UUID accessPointId, Integer validScanCount,
                                       Integer passCount, String capturedImageUrl, String note) {
    AccessPoint point = getAccessPoint(accessPointId);

    boolean tailgating = passCount != null && validScanCount != null && passCount > validScanCount;

    AccessEvent ev = AccessEvent.builder()
        .accessPointId(point.getId())
        .direction(AccessDirection.IN)
        .decision(AccessDecision.GRANTED)
        .tailgatingSuspected(tailgating)
        .validScanCount(validScanCount)
        .devicePassCount(passCount)
        .capturedImageUrl(capturedImageUrl)
        .occurredAt(LocalDateTime.now())
        .note(note)
        .build();
    ev.setGymId(point.getGymId());
    ev.setOrganisationId(point.getOrganisationId());
    ev = accessEventRepository.save(ev);

    if (tailgating) {
      eventPublisher.publishEvent(TailgatingSuspectedEvent.builder()
          .organisationId(point.getOrganisationId()).gymId(point.getGymId())
          .accessPointId(point.getId()).accessPointName(point.getName())
          .reason("device pass-count (" + passCount + ") exceeded valid scans (" + validScanCount + ")")
          .build());
    }
    return ev;
  }

  // ------------------------------------------------------------------
  // Credentials
  // ------------------------------------------------------------------

  @Transactional
  public IssuedCredential issueCredential(UUID memberId, CredentialType type, LocalDateTime expiresAt) {
    Member member = memberRepository.findById(memberId)
        .orElseThrow(() -> new ResourceNotFoundException("Member", memberId.toString()));

    CredentialType credType = type == null ? CredentialType.QR : type;
    String rawToken = generateToken(credType);

    AccessCredential credential = AccessCredential.builder()
        .memberId(memberId)
        .type(credType)
        .tokenHash(sha256(rawToken))
        .issuedAt(LocalDateTime.now())
        .expiresAt(expiresAt)
        .build();
    credential.setGymId(member.getGymId());
    credential.setOrganisationId(member.getOrganisationId());

    AccessCredential saved = accessCredentialRepository.save(credential);
    log.info("Issued {} credential {} for member {}", credType, saved.getId(), memberId);
    return new IssuedCredential(saved, rawToken);
  }

  @Transactional
  public void revokeCredential(UUID credentialId) {
    AccessCredential credential = accessCredentialRepository.findById(credentialId)
        .orElseThrow(() -> new ResourceNotFoundException("AccessCredential", credentialId.toString()));
    credential.setActive(false);
    accessCredentialRepository.save(credential);
    log.info("Revoked access credential {}", credentialId);
  }

  public List<AccessCredential> getCredentialsByMember(UUID memberId) {
    return accessCredentialRepository.findByMemberId(memberId);
  }

  // ------------------------------------------------------------------
  // Access points & events
  // ------------------------------------------------------------------

  @Transactional
  public AccessPoint createAccessPoint(AccessPoint point) {
    return accessPointRepository.save(point);
  }

  public List<AccessPoint> getAccessPointsByGym(UUID gymId) {
    return accessPointRepository.findByGymId(gymId);
  }

  public AccessPoint getAccessPoint(UUID id) {
    return accessPointRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("AccessPoint", id.toString()));
  }

  public List<AccessEvent> getEventsByGym(UUID gymId, boolean tailgatingOnly) {
    return tailgatingOnly
        ? accessEventRepository.findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(gymId)
        : accessEventRepository.findByGymIdOrderByOccurredAtDesc(gymId);
  }

  // ------------------------------------------------------------------
  // Helpers
  // ------------------------------------------------------------------

  private String generateToken(CredentialType type) {
    if (type == CredentialType.PIN) {
      return String.format("%06d", RANDOM.nextInt(1_000_000));
    }
    byte[] bytes = new byte[24];
    RANDOM.nextBytes(bytes);
    return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }

  private String sha256(String value) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
      StringBuilder hex = new StringBuilder(hash.length * 2);
      for (byte b : hash) {
        hex.append(Character.forDigit((b >> 4) & 0xF, 16));
        hex.append(Character.forDigit(b & 0xF, 16));
      }
      return hex.toString();
    } catch (java.security.NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 unavailable", e);
    }
  }
}
