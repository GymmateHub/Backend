package com.gymmate.access.internal.service;

import com.gymmate.access.internal.domain.AccessCredential;

/**
 * Result of issuing a credential. The raw token is returned exactly once
 * (only its hash is persisted) so it can be handed to the member.
 */
public record IssuedCredential(AccessCredential credential, String rawToken) {
}
