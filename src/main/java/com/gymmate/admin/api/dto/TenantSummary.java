package com.gymmate.admin.api.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class TenantSummary {
    private UUID id;
    private String name;
    private String slug;
    private String ownerName;
    private String contactEmail;
    private long gymCount;
    private long memberCount;
    private String plan;
    private String status;
    private LocalDateTime createdAt;
}
