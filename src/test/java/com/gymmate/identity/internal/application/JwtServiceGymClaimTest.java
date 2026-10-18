package com.gymmate.identity.internal.application;

import com.gymmate.identity.internal.application.port.TokenBlacklistRepository;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.shared.constants.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/**
 * GMH-007/014: a token without a gym claim must not report the organisation id as its gym.
 * That fallback made gym-scoped inserts fail their gym foreign key with an HTTP 500.
 */
class JwtServiceGymClaimTest {

    private JwtService jwtService;
    private User user;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(mock(TokenBlacklistRepository.class));
        ReflectionTestUtils.setField(jwtService, "jwtSecret",
                "Z3ltbWF0ZV9qd3Rfc2VjcmV0X2tleV9tdXN0X2JlX2F0X2xlYXN0XzMyX2J5dGVzX2xvbmdfMTIzNDU2");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3_600_000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 86_400_000L);

        user = User.builder().email("owner@example.com").role(UserRole.GYM_OWNER).build();
        user.setId(UUID.randomUUID());
        user.setOrganisationId(UUID.randomUUID());
    }

    @Test
    void tokenWithoutGymHasNoGym() {
        String token = jwtService.generateToken(user);

        assertThat(jwtService.extractGymId(token)).isNull();
        assertThat(jwtService.extractOrganisationId(token)).isEqualTo(user.getOrganisationId());
    }

    @Test
    void tokenWithGymCarriesThatGym() {
        UUID gymId = UUID.randomUUID();

        assertThat(jwtService.extractGymId(jwtService.generateToken(user, gymId))).isEqualTo(gymId);
    }
}
