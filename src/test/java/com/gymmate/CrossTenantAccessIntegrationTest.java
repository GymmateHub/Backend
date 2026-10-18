package com.gymmate;

import com.gymmate.identity.internal.application.JwtService;
import com.gymmate.identity.internal.application.port.UserRepository;
import com.gymmate.identity.internal.domain.User;
import com.gymmate.support.PostgresIntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Found by the end-to-end run: an owner of one organisation could read and change another
 * organisation's records by id (deactivating its gym, reading its leads), because Hibernate's
 * tenant filter does not apply to primary-key loads.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class CrossTenantAccessIntegrationTest extends PostgresIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository userRepository;
    @Autowired JwtService jwtService;

    UUID victimGymId;
    UUID victimLeadId;
    User rivalOwner;
    UUID rivalGymId;

    @BeforeEach
    void seedTwoOrganisations() {
        UUID victimOrg = seedOrganisation("Victim");
        victimGymId = seedGym(victimOrg, "Victim Gym");
        victimLeadId = UUID.randomUUID();
        jdbc.update("INSERT INTO leads (id, organisation_id, gym_id, first_name, last_name) VALUES (?, ?, ?, 'Vic', 'Lead')",
                victimLeadId, victimOrg, victimGymId);

        UUID rivalOrg = seedOrganisation("Rival");
        rivalGymId = seedGym(rivalOrg, "Rival Gym");
        UUID rivalId = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO users (id, organisation_id, email, first_name, last_name, role, status, email_verified)
                VALUES (?, ?, ?, 'Rita', 'Rival', 'GYM_OWNER', 'ACTIVE', true)
                """, rivalId, rivalOrg, "rival-" + rivalId + "@test.local");
        rivalOwner = userRepository.findById(rivalId).orElseThrow();
    }

    @Test
    void cannotReadAnotherOrganisationsGym() throws Exception {
        mockMvc.perform(asRival(get("/api/gyms/" + victimGymId))).andExpect(status().isNotFound());
    }

    @Test
    void cannotDeactivateAnotherOrganisationsGym() throws Exception {
        mockMvc.perform(asRival(patch("/api/gyms/" + victimGymId + "/deactivate"))).andExpect(status().isNotFound());

        assertThat(jdbc.queryForObject("SELECT is_active FROM gyms WHERE id = ?", Boolean.class, victimGymId)).isTrue();
    }

    @Test
    void cannotReadAnotherOrganisationsLead() throws Exception {
        mockMvc.perform(asRival(get("/api/leads/" + victimLeadId))).andExpect(status().isNotFound());
    }

    @Test
    void canStillReadOwnGym() throws Exception {
        mockMvc.perform(asRival(get("/api/gyms/" + rivalGymId))).andExpect(status().isOk());
    }

    @Test
    void unknownRouteIsNotFound() throws Exception {
        mockMvc.perform(asRival(get("/api/members/me/membership"))).andExpect(status().isNotFound());
    }

    private UUID seedOrganisation(String name) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO organisations (id, name, slug) VALUES (?, ?, ?)", id, name, "org-" + id);
        jdbc.update("""
                INSERT INTO subscriptions (organisation_id, tier_id, status, current_period_start, current_period_end)
                SELECT ?, id, 'ACTIVE', now(), now() + interval '30 days' FROM subscription_tiers WHERE name = 'starter'
                """, id);
        return id;
    }

    private UUID seedGym(UUID orgId, String name) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO gyms (id, organisation_id, name, slug) VALUES (?, ?, ?, ?)", id, orgId, name, "gym-" + id);
        return id;
    }

    private MockHttpServletRequestBuilder asRival(MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + jwtService.generateToken(rivalOwner, rivalGymId));
    }
}
