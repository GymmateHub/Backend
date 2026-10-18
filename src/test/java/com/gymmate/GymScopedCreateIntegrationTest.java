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
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * QA bugs GMH-007 (Add Member) and GMH-014 (Add Lead) returned HTTP 500. These tests drive
 * the real HTTP endpoints against PostgreSQL with a gym owner's token, the way the dashboard
 * does, so a schema, mapping or exception-mapping fault shows up as a failing status code.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class GymScopedCreateIntegrationTest extends PostgresIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository userRepository;
    @Autowired JwtService jwtService;

    UUID orgId;
    UUID gymId;
    User owner;

    @BeforeEach
    void seedOwnerWithGym() {
        orgId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        UUID ownerId = UUID.randomUUID();
        jdbc.update("INSERT INTO organisations (id, name, slug) VALUES (?, ?, ?)",
                orgId, "QA Org", "org-" + orgId);
        jdbc.update("INSERT INTO gyms (id, organisation_id, name, slug) VALUES (?, ?, ?, ?)",
                gymId, orgId, "QA Gym", "gym-" + gymId);
        // Sign-up provisions a starter subscription; every /api/** request is rate limited per subscription.
        jdbc.update("""
                INSERT INTO subscriptions (organisation_id, tier_id, status, current_period_start, current_period_end)
                SELECT ?, id, 'ACTIVE', now(), now() + interval '30 days' FROM subscription_tiers WHERE name = 'starter'
                """, orgId);
        jdbc.update("""
                INSERT INTO users (id, organisation_id, email, first_name, last_name, role, status, email_verified)
                VALUES (?, ?, ?, 'Gym', 'Owner', 'GYM_OWNER', 'ACTIVE', true)
                """, ownerId, orgId, "owner-" + ownerId + "@test.local");
        owner = userRepository.findById(ownerId).orElseThrow();
    }

    // ---- GMH-007: Add Member ----

    @Test
    void addMember_withGymId_createsMember() throws Exception {
        String email = "member-" + UUID.randomUUID() + "@test.local";

        mockMvc.perform(asOwner(post("/api/members"), null)
                        .content("""
                                {"gymId":"%s","email":"%s","firstName":"Ada","lastName":"Obi","phone":"+2348000000000"}
                                """.formatted(gymId, email)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        assertThat(jdbc.queryForObject(
                "SELECT count(*) FROM members m JOIN users u ON u.id = m.user_id WHERE u.email = ? AND m.gym_id = ?",
                Integer.class, email, gymId)).isEqualTo(1);
    }

    @Test
    void addMember_withoutGymId_fallsBackToTokenGym() throws Exception {
        String email = "member-" + UUID.randomUUID() + "@test.local";

        mockMvc.perform(asOwner(post("/api/members"), gymId)
                        .content("""
                                {"email":"%s","firstName":"Ada","lastName":"Obi"}
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    @Test
    void addMember_withoutAnyGym_isRejectedAsClientError() throws Exception {
        mockMvc.perform(asOwner(post("/api/members"), null)
                        .content("""
                                {"email":"nogym-%s@test.local","firstName":"Ada","lastName":"Obi"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMember_withExtraDashboardFields_isAccepted() throws Exception {
        // The dashboard's MemberCreateRequest type also carries dateOfBirth, gender and address.
        mockMvc.perform(asOwner(post("/api/members"), null)
                        .content("""
                                {"gymId":"%s","email":"extra-%s@test.local","firstName":"Ada","lastName":"Obi",
                                 "gender":"FEMALE","address":{"city":"Lagos"}}
                                """.formatted(gymId, UUID.randomUUID())))
                .andExpect(status().isCreated());
    }

    @Test
    void addMember_withNeitherEmailNorUserId_isRejectedAsClientError() throws Exception {
        mockMvc.perform(asOwner(post("/api/members"), null)
                        .content("""
                                {"gymId":"%s","firstName":"Ada","lastName":"Obi"}
                                """.formatted(gymId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addMember_withMalformedJson_isRejectedAsClientError() throws Exception {
        mockMvc.perform(asOwner(post("/api/members"), null).content("{\"gymId\": \"not-a-uuid\""))
                .andExpect(status().isBadRequest());
    }

    // ---- GMH-014: Add Lead ----

    @Test
    void addLead_withGymId_createsLead() throws Exception {
        mockMvc.perform(asOwner(post("/api/leads"), null)
                        .content("""
                                {"gymId":"%s","firstName":"Tunde","lastName":"Bello","email":"tunde@test.local",
                                 "phone":"+2348111111111","source":"Walk-in"}
                                """.formatted(gymId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.status").value("NEW"));

        assertThat(jdbc.queryForObject("SELECT count(*) FROM leads WHERE gym_id = ?", Integer.class, gymId))
                .isEqualTo(1);
    }

    @Test
    void addLead_withoutGymId_fallsBackToTokenGym() throws Exception {
        mockMvc.perform(asOwner(post("/api/leads"), gymId)
                        .content("""
                                {"firstName":"Tunde","lastName":"Bello"}
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void addLead_withoutAnyGym_isRejectedAsClientError() throws Exception {
        mockMvc.perform(asOwner(post("/api/leads"), null)
                        .content("""
                                {"firstName":"Tunde","lastName":"Bello"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addLead_withUnknownGym_isRejectedAsClientError() throws Exception {
        mockMvc.perform(asOwner(post("/api/leads"), null)
                        .content("""
                                {"gymId":"%s","firstName":"Tunde","lastName":"Bello"}
                                """.formatted(UUID.randomUUID())))
                .andExpect(status().is4xxClientError());
    }

    // ---- GMH-011: classes without a category must be listed (calendar dropdown) ----

    @Test
    void classWithoutCategory_isListedForItsGym() throws Exception {
        mockMvc.perform(asOwner(post("/api/classes"), gymId)
                        .content("""
                                {"gymId":"%s","name":"HIIT Power Hour","durationMinutes":45,"capacity":20}
                                """.formatted(gymId)))
                .andExpect(status().isCreated());

        mockMvc.perform(asOwner(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/api/classes/gym/" + gymId), gymId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("HIIT Power Hour"));
    }

    private MockHttpServletRequestBuilder asOwner(MockHttpServletRequestBuilder request, UUID tokenGymId) {
        return request
                .header("Authorization", "Bearer " + jwtService.generateToken(owner, tokenGymId))
                .contentType(MediaType.APPLICATION_JSON);
    }
}
