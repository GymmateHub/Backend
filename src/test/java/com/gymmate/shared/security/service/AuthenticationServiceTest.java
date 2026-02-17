package com.gymmate.shared.security.service;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.service.PasswordService;
import com.gymmate.user.api.dto.UnifiedRegistrationRequest;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;

import com.gymmate.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordService passwordService;

    @InjectMocks
    private AuthenticationService authenticationService;

    private UnifiedRegistrationRequest request;

    @BeforeEach
    void setUp() {
        request = new UnifiedRegistrationRequest(
                "test@example.com",
                "John",
                "Doe",
                "Password123",
                "1234567890",
                UserRole.MEMBER);
    }

    @Test
    void shouldRegisterOwnerWithoutOrganisation() {
        UnifiedRegistrationRequest ownerRequest = new UnifiedRegistrationRequest(
                "owner@example.com", "Owner", "Last", "Password123", "0987654321", UserRole.OWNER);

        when(userRepository.existsByEmail(ownerRequest.email())).thenReturn(false);
        when(passwordService.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        User result = authenticationService.register(ownerRequest);

        assertNotNull(result);
        assertEquals(UserRole.OWNER, result.getRole());
        assertNull(result.getOrganisationId());

    }

    @Test
    void shouldRegisterMemberWithTenantContext() {
        UUID tenantId = UUID.randomUUID();
        try (MockedStatic<TenantContext> mockedTenantContext = Mockito.mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(tenantId);

            when(passwordService.encode(anyString())).thenReturn("encodedPassword");
            when(userRepository.findByEmailAndOrganisationId(anyString(), any(UUID.class)))
                    .thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(UUID.randomUUID());
                return user;
            });

            User result = authenticationService.register(request);

            assertNotNull(result);
            assertEquals(UserRole.MEMBER, result.getRole());
            verify(userRepository).save(any(User.class));
        }
    }

    @Test
    void shouldThrowExceptionForMemberWithoutTenant() {
        try (MockedStatic<TenantContext> mockedTenantContext = Mockito.mockStatic(TenantContext.class)) {
            mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(null);

            DomainException exception = assertThrows(DomainException.class, () -> {
                authenticationService.register(request);
            });

            assertEquals("INVALID_REGISTRATION", exception.getErrorCode());
        }
    }

    @Test
    void shouldRegisterAdmin() {
        UnifiedRegistrationRequest adminRequest = new UnifiedRegistrationRequest(
                "admin@example.com", "Admin", "User", "Password123", "1231231234", UserRole.ADMIN);

        when(userRepository.existsByEmail(adminRequest.email())).thenReturn(false);
        when(passwordService.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId(UUID.randomUUID());
            return user;
        });

        User result = authenticationService.register(adminRequest);

        assertNotNull(result);
        assertEquals(UserRole.ADMIN, result.getRole());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void shouldThrowExceptionIfUserExistsForOwner() {
        UnifiedRegistrationRequest ownerRequest = new UnifiedRegistrationRequest(
                "owner@example.com", "Owner", "Last", "Password123", "0987654321", UserRole.OWNER);

        when(userRepository.existsByEmail(ownerRequest.email())).thenReturn(true);

        DomainException exception = assertThrows(DomainException.class, () -> {
            authenticationService.register(ownerRequest);
        });

        assertEquals("USER_ALREADY_EXISTS", exception.getErrorCode());
    }
}
