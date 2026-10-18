package com.gymmate.health.internal.infrastructure.web;

import com.gymmate.health.internal.application.port.FitnessGoalRepository;
import com.gymmate.health.internal.application.port.HealthMetricRepository;
import com.gymmate.health.internal.application.port.WorkoutLogRepository;
import com.gymmate.health.internal.domain.FitnessGoal;
import com.gymmate.health.internal.domain.HealthMetric;
import com.gymmate.health.internal.domain.WorkoutLog;
import com.gymmate.identity.api.IdentityApi;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.security.Permission;
import com.gymmate.shared.security.TenantAwareUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Health data belongs to one member: a member may read and record only their own metrics,
 * workouts and goals. Used from {@code @PreAuthorize} on the health controllers, e.g.
 * {@code @memberHealthAccess.canAccessMember(#memberId)}.
 *
 * <p>Callers with {@code health:manage} (owner, manager, trainer, platform admin) may access any
 * member of their organisation; tenant scoping does the rest. Callers with only
 * {@code self:service} (members) are limited to their own member record.
 * A record that does not exist passes here, so the endpoint can answer 404 instead of 403.
 */
@Component("memberHealthAccess")
@RequiredArgsConstructor
public class MemberHealthAccess {

    private final IdentityApi identityApi;
    private final FitnessGoalRepository fitnessGoalRepository;
    private final WorkoutLogRepository workoutLogRepository;
    private final HealthMetricRepository healthMetricRepository;

    public boolean canAccessMember(UUID memberId) {
        Optional<TenantAwareUserDetails> caller = caller();
        if (caller.isEmpty()) {
            return false;
        }
        if (has(caller.get(), Permission.HEALTH_MANAGE)) {
            return true;
        }
        return has(caller.get(), Permission.SELF_SERVICE)
                && memberId != null && memberId.equals(ownMemberId(caller.get().getUserId()));
    }

    public boolean canAccessGoal(UUID goalId) {
        return fitnessGoalRepository.findById(goalId).map(FitnessGoal::getMemberId).map(this::canAccessMember).orElse(true);
    }

    public boolean canAccessWorkout(UUID workoutId) {
        return workoutLogRepository.findById(workoutId).map(WorkoutLog::getMemberId).map(this::canAccessMember).orElse(true);
    }

    public boolean canAccessMetric(UUID metricId) {
        return healthMetricRepository.findById(metricId).map(HealthMetric::getMemberId).map(this::canAccessMember).orElse(true);
    }

    private UUID ownMemberId(UUID userId) {
        try {
            return identityApi.getMemberByUserId(userId).id();
        } catch (ResourceNotFoundException e) {
            return null;
        }
    }

    private static boolean has(TenantAwareUserDetails user, Permission permission) {
        return user.getAuthorities().stream().anyMatch(a -> permission.authority().equals(a.getAuthority()));
    }

    private static Optional<TenantAwareUserDetails> caller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getPrincipal() instanceof TenantAwareUserDetails details
                ? Optional.of(details)
                : Optional.empty();
    }
}
