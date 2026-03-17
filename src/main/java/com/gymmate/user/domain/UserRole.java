package com.gymmate.user.domain;

public enum UserRole {
  SUPER_ADMIN,
  ADMIN,
  GYM_OWNER,
  MANAGER,
  TRAINER,
  STAFF,
  MEMBER,

  /**
   * @deprecated Use {@link #GYM_OWNER} instead. Kept for backward compatibility during migration.
   */
  @Deprecated
  OWNER
}
