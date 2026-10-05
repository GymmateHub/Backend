package com.gymmate.organisation.internal.infrastructure.web;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.identity.api.dto.AccessTokenClaims;
import com.gymmate.identity.api.dto.TokenPair;
import com.gymmate.organisation.api.dto.GymSwitchResponse;
import com.gymmate.organisation.internal.application.GymService;
import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Gym context of the authenticated user's session (which gym the access token is scoped
 * to). Routes are unchanged from when they lived on identity's AuthController; token
 * reading/minting is delegated to identity's public API.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class GymContextController {

    private final GymService gymService;
    private final IdentityApi identityApi;

    @PostMapping("/switch-gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Switch gym context", description = "Switch to a different gym within your organisation")
    public ResponseEntity<ApiResponse<GymSwitchResponse>> switchGym(
            @PathVariable UUID gymId,
            @RequestHeader("Authorization") String authHeader) {

        AccessTokenClaims claims = identityApi.parseAccessToken(authHeader.substring(7));
        UUID organisationId = claims.organisationId();

        if (organisationId == null) {
            throw new DomainException("NO_ORGANISATION", "User is not associated with an organisation");
        }

        Gym gym = gymService.getGymById(gymId);
        if (!organisationId.equals(gym.getOrganisationId())) {
            throw new DomainException("GYM_ACCESS_DENIED",
                    "The selected gym does not belong to your organisation");
        }

        if (identityApi.findUser(claims.userId()).isEmpty()) {
            throw new DomainException("USER_NOT_FOUND", "User not found");
        }
        TokenPair tokens = identityApi.issueTokens(claims.userId(), gymId);

        GymSwitchResponse response = GymSwitchResponse.builder()
                .gymId(gym.getId())
                .gymName(gym.getName())
                .organisationId(organisationId)
                .accessToken(tokens.accessToken())
                .refreshToken(tokens.refreshToken())
                .message("Switched to gym: " + gym.getName())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response, "Gym context switched successfully"));
    }

    @GetMapping("/current-gym")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    @Operation(summary = "Get current gym context")
    public ResponseEntity<ApiResponse<GymSwitchResponse>> getCurrentGym(
            @RequestHeader("Authorization") String authHeader) {

        AccessTokenClaims claims = identityApi.parseAccessToken(authHeader.substring(7));
        UUID gymId = claims.gymId();
        UUID organisationId = claims.organisationId();

        if (gymId == null) {
            return ResponseEntity.ok(ApiResponse.success(
                    GymSwitchResponse.builder()
                            .organisationId(organisationId)
                            .message("No gym context set. Use /switch-gym/{gymId} to select a gym.")
                            .build()));
        }

        Gym gym = gymService.getGymById(gymId);
        GymSwitchResponse response = GymSwitchResponse.builder()
                .gymId(gym.getId())
                .gymName(gym.getName())
                .organisationId(organisationId)
                .message("Current gym: " + gym.getName())
                .build();

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
