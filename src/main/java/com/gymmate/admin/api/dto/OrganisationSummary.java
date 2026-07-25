package com.gymmate.admin.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class OrganisationSummary {
    private UUID id;
    private String name;
    private String slug;
    private String contactEmail;
    private String subscriptionPlan;
    private String subscriptionStatus;
    private long gymCount;
    private LocalDateTime createdAt;
}
