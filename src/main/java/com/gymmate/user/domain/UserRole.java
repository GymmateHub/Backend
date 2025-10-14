package com.gymmate.user.domain;

/**
 * Enumeration of user roles in the system.
 */
public enum UserRole {
  /**
   * Super admin with full platform access
   * */
  SUPER_ADMIN,  // Platform admin

  /**
   * Gym owner/manager with gym-level access
   * */
  GYM_OWNER,    // Gym owner/manager

  /**
   * Gym administrator with administrative privileges
   * */
  GYM_ADMIN,    // Gym administrator

  /**
   * Fitness trainer with access to training-related features
   * */
  TRAINER,      // Fitness trainer

  /**
   * Front desk staff with limited access
   * */
  STAFF,        // Front desk staff

  /**
   * Regular gym member with access to member features
   * */
  MEMBER        // Gym member
}
