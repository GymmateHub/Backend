package com.gymmate.shared.security.api;

import com.gymmate.gym.application.GymService;
import com.gymmate.notification.internal.infrastructure.web.SseEmitterRegistry;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.GlobalExceptionHandler;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.shared.security.dto.ChangePasswordRequest;
import com.gymmate.shared.security.service.AuthenticationService;
import com.gymmate.shared.security.service.JwtService;
import com.gymmate.user.application.InviteService;
import com.gymmate.user.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerChangePasswordTest {

    @Mock private AuthenticationService authenticationService;
    @Mock private GymService gymService;
    @Mock private JwtService jwtService;
    @Mock private UserRepository userRepository;
    @Mock private SseEmitterRegistry sseEmitterRegistry;
    @Mock private InviteService inviteService;

    @InjectMocks private AuthController controller;

    private static ChangePasswordRequest request(String current, String next) {
        ChangePasswordRequest r = new ChangePasswordRequest();
        r.setCurrentPassword(current);
        r.setNewPassword(next);
        return r;
    }

    @Test
    void changesThePasswordOfTheAuthenticatedUserOnly() {
        UUID userId = UUID.randomUUID();
        TenantAwareUserDetails principal = mock(TenantAwareUserDetails.class);
        when(principal.getUserId()).thenReturn(userId);

        ResponseEntity<ApiResponse<Void>> response =
                controller.changePassword(principal, request("OldPassword123!", "NewPassword456!"));

        verify(authenticationService).changePassword(userId, "OldPassword123!", "NewPassword456!");
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().isSuccess());
    }

    @Test
    void wrongCurrentPasswordIsReportedAsBadRequest_notAsAnExpiredSession() {
        // The service throws DomainException; the global handler must not turn it into a 401,
        // which the web client treats as "session expired" and answers with a token refresh/logout.
        DomainException wrong = new DomainException("INVALID_CURRENT_PASSWORD", "Current password is incorrect");

        ResponseEntity<ApiResponse<Object>> mapped = new GlobalExceptionHandler().handleDomainException(wrong);

        assertEquals(400, mapped.getStatusCode().value());
        assertEquals("Current password is incorrect", mapped.getBody().getMessage());
    }
}
