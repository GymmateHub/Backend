package com.gymmate.user.domain.events;

import org.springframework.context.ApplicationEvent;

import java.util.UUID;

public class MemberOnboardedEvent extends ApplicationEvent {

    private final UUID memberId;
    private final UUID gymId;
    private final String[] fitnessGoals;

    public MemberOnboardedEvent(Object source, UUID memberId, UUID gymId, String[] fitnessGoals) {
        super(source);
        this.memberId = memberId;
        this.gymId = gymId;
        this.fitnessGoals = fitnessGoals;
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
