package com.gymmate.onboarding.internal.application;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.identity.api.dto.MemberRegistrationRequest;
import com.gymmate.identity.api.dto.NewUserRegistration;
import com.gymmate.identity.api.dto.OwnerRegistrationRequest;
import com.gymmate.organisation.api.OrganisationApi;
import com.gymmate.organisation.api.dto.GymSummary;
import com.gymmate.organisation.api.dto.InitialGymCommand;
import com.gymmate.shared.constants.GymStatus;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.exception.DomainException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** Registration flows (moved here from AuthenticationServiceTest with the onboarding module split). */
@ExtendWith(MockitoExtension.class)
class OnboardingServiceTest {

    @Mock
    private IdentityApi identityApi;
    @Mock
    private OrganisationApi organisationApi;

    @InjectMocks
    private OnboardingService onboardingService;

    private final UUID ownerId = UUID.randomUUID();
    private final UUID organisationId = UUID.randomUUID();

    private OwnerRegistrationRequest ownerRequest(String orgName, String gymName, String timezone, String country) {
        return new OwnerRegistrationRequest(
                "owner@example.com", "Owner", "User", "Password123!", "1234567890", orgName, gymName, timezone, country);
    }

    private void stubOwnerHappyPath() {
        when(identityApi.emailExists("owner@example.com")).thenReturn(false);
        when(identityApi.registerUser(any(NewUserRegistration.class))).thenReturn(ownerId);
        when(organisationApi.createHub(anyString(), anyString(), eq(ownerId))).thenReturn(organisationId);
    }

    @Test
    void shouldRegisterOwner() {
        stubOwnerHappyPath();

        UUID result = onboardingService.registerOwner(ownerRequest("My Org", "My Gym", "UTC", "US"));

        assertEquals(ownerId, result);
        ArgumentCaptor<NewUserRegistration> user = ArgumentCaptor.forClass(NewUserRegistration.class);
        verify(identityApi).registerUser(user.capture());
        assertEquals(UserRole.GYM_OWNER, user.getValue().role());
        assertNull(user.getValue().organisationId());
        verify(organisationApi).createHub("My Org", "owner@example.com", ownerId);
        verify(organisationApi).checkCanCreateGym(organisationId);
        ArgumentCaptor<InitialGymCommand> gym = ArgumentCaptor.forClass(InitialGymCommand.class);
        verify(organisationApi).createInitialGym(gym.capture());
        assertEquals("My Gym", gym.getValue().name());
        assertEquals(organisationId, gym.getValue().organisationId());
    }

    @Test
    void shouldRegisterOwnerWithNullOptionalFields() {
        stubOwnerHappyPath();

        onboardingService.registerOwner(ownerRequest(null, null, null, null));

        verify(organisationApi).createHub("Owner's Organisation", "owner@example.com", ownerId);
        ArgumentCaptor<InitialGymCommand> gym = ArgumentCaptor.forClass(InitialGymCommand.class);
        verify(organisationApi).createInitialGym(gym.capture());
        assertEquals("Owner's Gym", gym.getValue().name());
        assertEquals("UTC", gym.getValue().timezone());
        assertEquals("United States", gym.getValue().country());
    }

    @Test
    void shouldThrowExceptionIfOwnerExists() {
        when(identityApi.emailExists("owner@example.com")).thenReturn(true);

        DomainException exception = assertThrows(DomainException.class,
                () -> onboardingService.registerOwner(ownerRequest("My Org", "My Gym", "UTC", "US")));

        assertEquals("USER_ALREADY_EXISTS", exception.getErrorCode());
        verify(identityApi, never()).registerUser(any());
        verifyNoInteractions(organisationApi);
    }

    @Test
    void shouldRegisterMember() {
        MemberRegistrationRequest request = new MemberRegistrationRequest(
                "member@example.com", "Member", "User", "Password123!", "1234567890", "gym-slug");
        UUID gymId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        when(organisationApi.getOrganisationIdBySlug("gym-slug")).thenReturn(organisationId);
        when(organisationApi.getActiveGyms(organisationId)).thenReturn(List.of(new GymSummary(
                gymId, organisationId, "Gym", "gym@example.com", null, null, "UTC", "USD", GymStatus.ACTIVE, true,
                null, false, false, false, null)));
        when(identityApi.emailExists(request.email())).thenReturn(false);
        when(identityApi.registerUser(any(NewUserRegistration.class))).thenReturn(userId);

        UUID result = onboardingService.registerMember(request);

        assertEquals(userId, result);
        ArgumentCaptor<NewUserRegistration> user = ArgumentCaptor.forClass(NewUserRegistration.class);
        verify(identityApi).registerUser(user.capture());
        assertEquals(UserRole.MEMBER, user.getValue().role());
        assertEquals(organisationId, user.getValue().organisationId());
        verify(identityApi).createMemberProfile(userId, gymId);
    }

    @Test
    void shouldThrowExceptionForMemberWithoutGymSlug() {
        MemberRegistrationRequest request = new MemberRegistrationRequest(
                "member@example.com", "Member", "User", "Password123!", "1234567890", null);

        DomainException exception = assertThrows(DomainException.class,
                () -> onboardingService.registerMember(request));

        assertEquals("INVALID_REQUEST", exception.getErrorCode());
    }

    // ---- currency defaulting at registration ----

    private String registerOwnerWithCountry(String country) {
        stubOwnerHappyPath();
        onboardingService.registerOwner(ownerRequest("My Org", "My Gym", "UTC", country));
        ArgumentCaptor<InitialGymCommand> captor = ArgumentCaptor.forClass(InitialGymCommand.class);
        verify(organisationApi).createInitialGym(captor.capture());
        return captor.getValue().currency();
    }

    @Test
    void registerOwner_nigerianGymGetsNgnCurrency() {
        assertEquals("NGN", registerOwnerWithCountry("Nigeria"));
    }

    @Test
    void registerOwner_missingCountryDefaultsToPlatformCurrency() {
        assertEquals("NGN", registerOwnerWithCountry(null));
    }

    @Test
    void registerOwner_usGymKeepsUsd() {
        assertEquals("USD", registerOwnerWithCountry("US"));
    }
}
