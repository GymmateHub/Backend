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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Security gaps from the role and permission review:
 * <ol>
 *   <li>gym registration was public and trusted an {@code ownerId} from the body;</li>
 *   <li>any user could read any member's health data, and members could write each other's;</li>
 *   <li>an unauthenticated endpoint sent test emails (now covered by the owner-only SMTP test).</li>
 * </ol>
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class SecurityGapsIntegrationTest extends PostgresIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired JdbcTemplate jdbc;
    @Autowired UserRepository userRepository;
    @Autowired JwtService jwtService;

    UUID orgId;
    UUID gymId;
    User owner;
    User memberA;
    UUID memberAId;
    UUID memberBId;

    @BeforeEach
    void seed() {
        orgId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        jdbc.update("INSERT INTO organisations (id, name, slug, max_gyms) VALUES (?, 'Org', ?, 5)", orgId, "org-" + orgId);
        jdbc.update("INSERT INTO gyms (id, organisation_id, name, slug) VALUES (?, ?, 'Gym', ?)", gymId, orgId, "gym-" + gymId);
        jdbc.update("""
                INSERT INTO subscriptions (organisation_id, tier_id, status, current_period_start, current_period_end)
                SELECT ?, id, 'ACTIVE', now(), now() + interval '30 days' FROM subscription_tiers WHERE name = 'starter'
                """, orgId);
        owner = seedUser("GYM_OWNER");
        memberA = seedUser("MEMBER");
        memberAId = seedMember(memberA.getId());
        memberBId = seedMember(seedUser("MEMBER").getId());
    }

    // ---- 1. gym registration ----

    @Test
    void gymRegistration_requiresLogin() throws Exception {
        mockMvc.perform(post("/api/gyms/register").contentType(MediaType.APPLICATION_JSON).content(gymBody(owner.getId())))
                .andExpect(status().is4xxClientError());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM gyms WHERE organisation_id = ?", Integer.class, orgId)).isEqualTo(1);
    }

    @Test
    void gymRegistration_registersForTheLoggedInOwner() throws Exception {
        mockMvc.perform(as(owner, post("/api/gyms/register")).content(gymBody(null)))
                .andExpect(status().isCreated());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM gyms WHERE organisation_id = ?", Integer.class, orgId)).isEqualTo(2);
    }

    @Test
    void gymRegistration_ownerCannotRegisterForSomeoneElse() throws Exception {
        UUID otherOrg = UUID.randomUUID();
        jdbc.update("INSERT INTO organisations (id, name, slug) VALUES (?, 'Other', ?)", otherOrg, "org-" + otherOrg);
        UUID otherOwner = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO users (id, organisation_id, email, first_name, last_name, role, status, email_verified)
                VALUES (?, ?, ?, 'O', 'O', 'GYM_OWNER', 'ACTIVE', true)
                """, otherOwner, otherOrg, "other-" + otherOwner + "@test.local");

        mockMvc.perform(as(owner, post("/api/gyms/register")).content(gymBody(otherOwner)))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM gyms WHERE organisation_id = ?", Integer.class, otherOrg)).isZero();
    }

    @Test
    void gymRegistration_membersCannotRegisterGyms() throws Exception {
        mockMvc.perform(as(memberA, post("/api/gyms/register")).content(gymBody(null)))
                .andExpect(status().isForbidden());
    }

    // ---- 2. health data scoped to the member ----

    @Test
    void member_readsOwnHealthData() throws Exception {
        mockMvc.perform(as(memberA, get("/api/workouts/member/" + memberAId))).andExpect(status().isOk());
        mockMvc.perform(as(memberA, get("/api/health-metrics/all").param("memberId", memberAId.toString()))).andExpect(status().isOk());
        mockMvc.perform(as(memberA, get("/api/fitness-goals/member/" + memberAId))).andExpect(status().isOk());
    }

    @Test
    void member_cannotReadAnotherMembersHealthData() throws Exception {
        mockMvc.perform(as(memberA, get("/api/workouts/member/" + memberBId))).andExpect(status().isForbidden());
        mockMvc.perform(as(memberA, get("/api/health-metrics/all").param("memberId", memberBId.toString()))).andExpect(status().isForbidden());
        mockMvc.perform(as(memberA, get("/api/fitness-goals/member/" + memberBId))).andExpect(status().isForbidden());
        mockMvc.perform(as(memberA, get("/api/health-dashboard/member/" + memberBId))).andExpect(status().isForbidden());
    }

    @Test
    void member_recordsOwnMetricButNotAnotherMembers() throws Exception {
        mockMvc.perform(as(memberA, post("/api/health-metrics")).param("gymId", gymId.toString()).content(metricBody(memberAId)))
                .andExpect(status().isCreated());
        mockMvc.perform(as(memberA, post("/api/health-metrics")).param("gymId", gymId.toString()).content(metricBody(memberBId)))
                .andExpect(status().isForbidden());
        assertThat(jdbc.queryForObject("SELECT count(*) FROM health_metrics WHERE member_id = ?", Integer.class, memberBId)).isZero();
    }

    @Test
    void gymOwner_readsAnyMembersHealthData() throws Exception {
        mockMvc.perform(as(owner, get("/api/workouts/member/" + memberBId))).andExpect(status().isOk());
    }

    @Test
    void anonymous_cannotReadHealthData() throws Exception {
        mockMvc.perform(get("/api/workouts/member/" + memberAId)).andExpect(status().is4xxClientError());
    }

    // ---- 3. no public test-email endpoint ----

    @Test
    void testEmailEndpointIsGone() throws Exception {
        mockMvc.perform(get("/api/notifications/test-email")).andExpect(status().is4xxClientError());
        // The path now falls through to GET /api/notifications/{id}, which rejects it as a bad id.
        mockMvc.perform(as(owner, get("/api/notifications/test-email"))).andExpect(status().is4xxClientError());
    }

    private User seedUser(String role) {
        UUID id = UUID.randomUUID();
        jdbc.update("""
                INSERT INTO users (id, organisation_id, email, first_name, last_name, role, status, email_verified)
                VALUES (?, ?, ?, 'T', 'User', ?, 'ACTIVE', true)
                """, id, orgId, role.toLowerCase() + "-" + id + "@test.local", role);
        return userRepository.findById(id).orElseThrow();
    }

    private UUID seedMember(UUID userId) {
        UUID id = UUID.randomUUID();
        jdbc.update("INSERT INTO members (id, organisation_id, gym_id, user_id) VALUES (?, ?, ?, ?)", id, orgId, gymId, userId);
        return id;
    }

    private static String gymBody(UUID ownerId) {
        return """
                {%s"name":"Second Gym","contactEmail":"gym2@test.local","contactPhone":"+2348000000000"}
                """.formatted(ownerId == null ? "" : "\"ownerId\":\"" + ownerId + "\",");
    }

    private static String metricBody(UUID memberId) {
        return """
                {"memberId":"%s","metricType":"WEIGHT","value":72.5,"unit":"kg"}
                """.formatted(memberId);
    }

    private MockHttpServletRequestBuilder as(User user, MockHttpServletRequestBuilder request) {
        return request.header("Authorization", "Bearer " + jwtService.generateToken(user, gymId))
                .contentType(MediaType.APPLICATION_JSON);
    }
}
