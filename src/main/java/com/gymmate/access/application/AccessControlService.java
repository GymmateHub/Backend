package com.gymmate.access.application;

import com.gymmate.access.domain.AccessLog;
import com.gymmate.access.infrastructure.AccessLogRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.user.application.MemberService;
import com.gymmate.user.domain.Member;
import com.gymmate.shared.constants.MemberStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccessControlService {

    private final AccessLogRepository accessLogRepository;
    private final MemberService memberService;

    // Time window for time-based lockout (in minutes)
    private static final int LOCKOUT_MINUTES = 15;

    /**
     * Process a physical access request at a gym.
     */
    @Transactional
    public AccessLog processAccessRequest(UUID memberId, UUID gymId, AccessLog.AccessDirection requestedDirection, String accessMethod) {
        Member member = memberService.findById(memberId);

        // 1. Verify membership status
        if (member.getStatus() != MemberStatus.ACTIVE) {
            return recordAccessLog(memberId, gymId, requestedDirection, AccessLog.AccessStatus.DENIED_MEMBERSHIP, accessMethod, "Membership is not active");
        }

        Optional<AccessLog> lastAccess = accessLogRepository.findTopByMemberIdOrderByAccessTimeDesc(memberId);

        if (lastAccess.isPresent()) {
            AccessLog previous = lastAccess.get();

            // 2. Strict Anti-Passback
            if (previous.getDirection() == AccessLog.AccessDirection.ENTRY && requestedDirection == AccessLog.AccessDirection.ENTRY) {
                // Determine if they just forgot to check out (time based heuristic)
                if (previous.getAccessTime().plusHours(12).isAfter(LocalDateTime.now())) {
                    return recordAccessLog(memberId, gymId, requestedDirection, AccessLog.AccessStatus.DENIED_PASSBACK, accessMethod, "User is already checked in");
                }
            }

            // 3. Time-based Lockout (if entry scanner used again too quickly)
            if (previous.getDirection() == requestedDirection && 
                previous.getAccessTime().plusMinutes(LOCKOUT_MINUTES).isAfter(LocalDateTime.now())) {
                return recordAccessLog(memberId, gymId, requestedDirection, AccessLog.AccessStatus.DENIED_LOCKOUT, accessMethod, "Please wait " + LOCKOUT_MINUTES + " minutes between scans");
            }
        }

        // Access Granted
        return recordAccessLog(memberId, gymId, requestedDirection, AccessLog.AccessStatus.GRANTED, accessMethod, null);
    }

    /**
     * Log a tailgating alert (usually triggered by hardware sensors e.g., turnstiles detecting 2 bodies on 1 scan)
     */
    @Transactional
    public AccessLog logTailgatingAlert(UUID memberId, UUID gymId, String accessMethod) {
        return recordAccessLog(memberId, gymId, AccessLog.AccessDirection.ENTRY, AccessLog.AccessStatus.ALERT_TAILGATING, accessMethod, "Hardware sensor detected potential tailgating");
    }

    private AccessLog recordAccessLog(UUID memberId, UUID gymId, AccessLog.AccessDirection direction, AccessLog.AccessStatus status, String method, String denialReason) {
        AccessLog log = AccessLog.builder()
                .memberId(memberId)
                .accessTime(LocalDateTime.now())
                .direction(direction)
                .status(status)
                .accessMethod(method)
                .denialReason(denialReason)
                .build();
        log.setGymId(gymId);
        
        return accessLogRepository.save(log);
    }
}
