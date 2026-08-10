package com.gymmate.user.domain.events;

import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class MemberOnboardedEvent extends ApplicationEvent implements TenantAwareEvent {

    private final UUID organisationId;
    private final UUID memberId;
    private final UUID gymId;
    private final String[] fitnessGoals;

    public MemberOnboardedEvent(Object source, UUID organisationId, UUID memberId, UUID gymId, String[] fitnessGoals) {
        super(source);
        this.organisationId = organisationId;
        this.memberId = memberId;
        this.gymId = gymId;
        this.fitnessGoals = fitnessGoals;
    }

    @Override
    public TenantIdentity getTenantIdentity() {
        return TenantIdentity.of(organisationId, gymId);
    }

    public UUID getOrganisationId() {
        return organisationId;
    }

    public UUID getMemberId() {
        return memberId;
    }

    public UUID getGymId() {
        return gymId;
    }

    public String[] getFitnessGoals() {
        return fitnessGoals;
    }
}
