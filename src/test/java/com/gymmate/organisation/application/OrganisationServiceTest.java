package com.gymmate.organisation.application;

import com.gymmate.organisation.domain.Organisation;
import com.gymmate.organisation.infrastructure.OrganisationRepository;
import com.gymmate.subscription.application.SubscriptionService;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrganisationServiceTest {

    @Mock
    private OrganisationRepository organisationRepository;
    @Mock
    private SubscriptionService subscriptionService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OrganisationService organisationService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = User.builder()
                .email("owner@example.com")
                .build();
        owner.setId(UUID.randomUUID());
        owner.setOrganisationId(null);
    }

    @Test
    void shouldCreateHubWithSubscriptionAndLinkUser() {
        // Arrange
        String name = "Gym Hub";
        String email = "contact@gymhub.com";
        String slug = "gym-hub";

        // Mock repo to return false (slug doesn't exist)
        lenient().when(organisationRepository.existsBySlug(anyString())).thenReturn(false);

        // Mock findById for verify logic inside link (though assignOwner uses
        // repo.findById? No, createHub -> assignOwner -> repo.findById)
        // Wait, assignOwner inside OrganisationService uses repo.findById.
        // Let's check assignOwner implementation again.
        // assignOwner calls organisationRepository.findById(organisationId).
        // BUT createHub calls createOrganisation which returns the saved Organisation.
        // Then assignOwner is called with the ID.
        // So we need to mock findById to return the organization that was just "saved".

        // Actually, let's look at assignOwner implementation in OrganisationService.
        // public void assignOwner(UUID organisationId, UUID userId) {
        // Organisation organisation =
        // organisationRepository.findById(organisationId)...
        // }
        // So yes, we need to mock findById.

        // However, standard mockito when(...) setup needs to happen before execution.
        // Since the ID is generated inside the 'save' mock, we can't easily match
        // implementation details unless we fix the ID.
        UUID fixedOrgId = UUID.randomUUID();
        when(organisationRepository.save(any(Organisation.class))).thenAnswer(invocation -> {
            Organisation org = invocation.getArgument(0);
            org.setId(fixedOrgId);
            return org;
        });

        // Mock findById to return the organisation (needed for assignOwner)
        when(organisationRepository.findById(fixedOrgId)).thenAnswer(invocation -> {
            Organisation org = Organisation.builder()
                    .name(name)
                    .slug(slug)
                    .contactEmail(email)
                    .build();
            org.setId(fixedOrgId);
            return java.util.Optional.of(org);
        });

        // Act
        Organisation result = organisationService.createHub(name, email, owner);

        // Assert
        assertNotNull(result);
        assertEquals(fixedOrgId, result.getId());

        // Verify interactions

        verify(subscriptionService).createSubscription(eq(fixedOrgId), eq("starter"), eq(true));

        // Verify assignOwner logic (it calls save on repo again)
        verify(organisationRepository, times(2)).save(any(Organisation.class));

        // Verify User update
        assertEquals(fixedOrgId, owner.getOrganisationId());
        verify(userRepository).save(owner);
    }

    @Test
    void shouldRetrySlugGenerationOnCollision() {
        // Arrange
        String name = "Test Gym";
        String email = "contact@test.com";
        String initialSlug = "test-gym";

        // Mock slug does not exist initially
        lenient().when(organisationRepository.existsBySlug(anyString())).thenReturn(false);

        // First save throws exception, second succeeds
        when(organisationRepository.save(any(Organisation.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate slug"))
                .thenAnswer(i -> {
                    Organisation org = i.getArgument(0);
                    // Verify slug is modified (has suffix)
                    assertNotEquals(initialSlug, org.getSlug());
                    assertTrue(org.getSlug().startsWith(initialSlug + "-"));
                    org.setId(UUID.randomUUID());
                    return org;
                });

        // Act
        Organisation result = organisationService.createOrganisation(name, initialSlug, email);

        // Assert
        assertNotNull(result);
        verify(organisationRepository, times(2)).save(any(Organisation.class));
    }
}
