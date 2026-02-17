package com.gymmate.shared.security.invite;

/**
 * Status lifecycle for user invites.
 *
 * State transitions:
 * - PENDING → ACCEPTED (invite/accept called with valid token)
 * - PENDING → EXPIRED  (scheduled job: expires_at < NOW())
 * - PENDING → REVOKED  (OWNER/ADMIN cancels invite from dashboard)
 * - EXPIRED → PENDING  (invite/resend issues new token, resets expiry)
 */
public enum InviteStatus {
    /**
     * Invite sent, awaiting user to accept and set password.
     */
    PENDING,

    /**
     * User has accepted the invite and created their account.
     */
    ACCEPTED,

    /**
     * Invite expired (expires_at < NOW()). Can be resent.
     */
    EXPIRED,

    /**
     * Invite was cancelled by OWNER/ADMIN. Cannot be resent.
     */
    REVOKED
}

