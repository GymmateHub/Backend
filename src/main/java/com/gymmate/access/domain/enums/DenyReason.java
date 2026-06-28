package com.gymmate.access.domain.enums;

/**
 * Standardised reasons an entry is denied. Mirrors competitor (GymMaster)
 * denial checklist plus the anti-tailgating block.
 */
public enum DenyReason {
  INVALID_CREDENTIAL,
  NO_ACTIVE_MEMBERSHIP,
  SUSPENDED_OR_FROZEN,
  INCOMPLETE_SIGNUP,
  OVERDUE_OVER_LIMIT,
  VISITS_EXHAUSTED,
  NO_DOOR_BENEFIT,
  OUTSIDE_ACCESS_TIMES,
  STOP_AT_GATE_TASK,
  TAILGATING_BLOCKED
}
