package com.gymmate.access.application;

import com.gymmate.access.domain.AccessCredential;

/**
 * Result of issuing a credential. The raw token is returned exactly once
 * (only its hash is persisted) so it can be handed to the member.
 */
public record IssuedCredential(AccessCredential credential, String rawToken) {
}
