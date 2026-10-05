package com.gymmate.organisation.internal.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymmate.organisation.internal.application.GymService;
import com.gymmate.organisation.api.dto.CreateHubRequest;
import com.gymmate.organisation.internal.application.OrganisationLimitService;
import com.gymmate.organisation.internal.application.OrganisationService;
import com.gymmate.organisation.internal.domain.Organisation;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.identity.api.IdentityApi;
import com.gymmate.identity.internal.application.IdentityApiTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class OrganisationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private OrganisationService organisationService;

    @Mock
    private OrganisationLimitService limitService;

    @Mock
    private GymService gymService;

    @Mock
    private IdentityApi identityApi;

    @InjectMocks
    private OrganisationController organisationController;

    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(organisationController).build();
    }

    @Test
    void shouldCreateHubWithAuthenticatedUser() throws Exception {
        // Arrange
        CreateHubRequest request = CreateHubRequest.builder()
                .name("Test Gym")
                .contactEmail("test@gym.com")
                .build();

        UUID userId = UUID.randomUUID();
        UUID orgId = UUID.randomUUID();

        // Mock Security Context
        Authentication authentication = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        TenantAwareUserDetails userDetails = mock(TenantAwareUserDetails.class);

        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(userDetails);
        when(userDetails.getUserId()).thenReturn(userId);
        SecurityContextHolder.setContext(securityContext);

        // Mock identity facade: authenticated user without an organisation yet
        User user = User.builder().email("test@gym.com").build();
        user.setId(userId);
        user.setOrganisationId(null);
        when(identityApi.findUser(userId)).thenReturn(Optional.of(IdentityApiTestSupport.summary(user)));

        // Mock Service
        Organisation org = Organisation.builder()
                .name("Test Gym")
                .contactEmail("test@gym.com")
                .slug("test-gym")
                .build();
        org.setId(orgId);

        when(organisationService.createHub(eq("Test Gym"), eq("test@gym.com"), eq(userId))).thenReturn(org);

        // Act & Assert
        mockMvc.perform(post("/api/organisations/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Test Gym"))
                .andExpect(jsonPath("$.data.id").value(orgId.toString()));

        verify(organisationService).createHub(eq("Test Gym"), eq("test@gym.com"), eq(userId));

        // Cleanup
        SecurityContextHolder.clearContext();
    }
}
