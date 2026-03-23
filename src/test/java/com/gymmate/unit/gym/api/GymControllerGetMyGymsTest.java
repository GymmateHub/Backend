package com.gymmate.unit.gym.api;

import com.gymmate.gym.api.GymController;
import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for GymController.getMyGyms after removing the JWT fallback pattern.
 * Validates that the endpoint now uses TenantContext.requireCurrentTenantId().
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GymController getMyGyms Tests")
class GymControllerGetMyGymsTest {

    @Mock
    private GymService gymService;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private GymController gymController;

    private UUID organisationId;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("getMyGyms")
    class GetMyGyms {

        @Test
        @DisplayName("Should return gyms when tenant context is set")
        void shouldReturnGymsWhenTenantContextSet() {
            Gym gym = new Gym("Test Gym", "Desc", "test@gym.com", "+1234567890", UUID.randomUUID());
            gym.setId(UUID.randomUUID());
            gym.setOrganisationId(organisationId);

            when(gymService.getGymsByOrganisation(organisationId)).thenReturn(List.of(gym));

            try (MockedStatic<TenantContext> mocked = mockStatic(TenantContext.class)) {
                mocked.when(TenantContext::requireCurrentTenantId).thenReturn(organisationId);

                var response = gymController.getMyGyms();

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().isSuccess()).isTrue();
                verify(gymService).getGymsByOrganisation(organisationId);
            }
        }

        @Test
        @DisplayName("Should throw when tenant context is not set")
        void shouldThrowWhenTenantContextNotSet() {
            try (MockedStatic<TenantContext> mocked = mockStatic(TenantContext.class)) {
                mocked.when(TenantContext::requireCurrentTenantId)
                        .thenThrow(new IllegalStateException("No tenant context set for current thread"));

                assertThatThrownBy(() -> gymController.getMyGyms())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("No tenant context");
            }
        }

        @Test
        @DisplayName("Should not require Authorization header parameter")
        void shouldNotRequireAuthorizationHeader() throws NoSuchMethodException {
            // getMyGyms() should have no parameters (Authorization header removed)
            var method = GymController.class.getMethod("getMyGyms");
            assertThat(method.getParameterCount()).isZero();
        }
    }
}

