package com.gymmate.shared.dto;

import com.gymmate.shared.constants.AuditEventType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
// Event entity
public class SecurityAuditEvent {
  private AuditEventType eventType;
  private UUID userId;
  private String email;
  private String ipAddress;
  private String userAgent;
  private Instant timestamp;
  private String details;
  private Map<String, Object> metadata;
}
