package com.gymmate.identity.api.event;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;

import java.util.UUID;

/**
 * Published when a member profile gets fitness goals (onboarding); the AI trainer generates a
 * personalised plan from it. A plain, JSON-serialisable payload (no Spring event source) so it
 * can be stored in the event publication registry.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MemberOnboardedEvent implements TenantAwareEvent {

    private final UUID organisationId;
    private final UUID memberId;
    private final UUID gymId;
    private final String[] fitnessGoals;

    @JsonCreator
    public MemberOnboardedEvent(@JsonProperty("organisationId") UUID organisationId,
                                @JsonProperty("memberId") UUID memberId,
                                @JsonProperty("gymId") UUID gymId,
                                @JsonProperty("fitnessGoals") String[] fitnessGoals) {
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
