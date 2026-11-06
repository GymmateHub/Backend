package com.gymmate.unit.user.api;

import com.gymmate.organisation.internal.application.OrganisationLimitService;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.CurrentUser;
import com.gymmate.identity.internal.infrastructure.web.MemberController;
import com.gymmate.identity.internal.application.dto.MemberResponse;
import com.gymmate.identity.internal.application.MemberService;
import com.gymmate.identity.internal.domain.Member;
import com.gymmate.shared.constants.MemberStatus;
import com.gymmate.identity.internal.application.port.MemberRepository;
import com.gymmate.identity.internal.application.port.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MemberController tenant isolation.
 * Validates that getMemberById, getMemberByUserId, and getMemberByMembershipNumber
 * properly reject cross-tenant access.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MemberController Tenant Isolation Tests")
class MemberControllerTenantTest {

    @Mock
    private MemberService memberService;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private OrganisationLimitService limitService;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private MemberController memberController;

    private UUID orgA;
    private UUID orgB;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        orgA = UUID.randomUUID();
        orgB = UUID.randomUUID();
        memberId = UUID.randomUUID();
    }

    private Member buildMember(UUID organisationId) {
        Member member = Member.builder()
                .userId(UUID.randomUUID())
                .membershipNumber("MEM-001")
                .joinDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .build();
        member.setId(memberId);
        member.setOrganisationId(organisationId);
        return member;
    }

    @Nested
    @DisplayName("getMemberById")
    class GetMemberById {

        @Test
        @DisplayName("Should return member when same organisation")
        void shouldReturnMemberWhenSameOrg() {
            Member member = buildMember(orgA);
            when(memberService.findById(memberId)).thenReturn(member);

            try (MockedStatic<TenantContext> mocked = mockStatic(TenantContext.class)) {
                mocked.when(TenantContext::getCurrentTenantId).thenReturn(orgA);

                ResponseEntity<ApiResponse<MemberResponse>> response = memberController.getMemberById(memberId);

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().isSuccess()).isTrue();
            }
        }

        @Test
        @DisplayName("Should return 403 when different organisation")
        void shouldReturn403WhenDifferentOrg() {
            Member member = buildMember(orgA);
            when(memberService.findById(memberId)).thenReturn(member);

            try (MockedStatic<TenantContext> mocked = mockStatic(TenantContext.class)) {
                mocked.when(TenantContext::getCurrentTenantId).thenReturn(orgB);

                ResponseEntity<ApiResponse<MemberResponse>> response = memberController.getMemberById(memberId);

                assertThat(response.getStatusCode().value()).isEqualTo(403);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().isSuccess()).isFalse();
            }
        }
    }

    @Nested
    @DisplayName("getMemberByUserId")
    class GetMemberByUserId {

        @Test
        @DisplayName("Should return 403 when cross-tenant access via userId")
        void shouldReturn403ForCrossTenantByUserId() {
            UUID userId = UUID.randomUUID();
            Member member = buildMember(orgA);
            when(memberService.findByUserId(userId)).thenReturn(member);

            try (MockedStatic<TenantContext> mocked = mockStatic(TenantContext.class)) {
                mocked.when(TenantContext::getCurrentTenantId).thenReturn(orgB);

                ResponseEntity<ApiResponse<MemberResponse>> response = memberController.getMemberByUserId(userId);

                assertThat(response.getStatusCode().value()).isEqualTo(403);
            }
        }
    }

    @Nested
    @DisplayName("getMemberByMembershipNumber")
    class GetMemberByMembershipNumber {

        @Test
        @DisplayName("Should return 403 when cross-tenant access via membership number")
        void shouldReturn403ForCrossTenantByMembershipNumber() {
            Member member = buildMember(orgA);
            when(memberService.findByMembershipNumber("MEM-001")).thenReturn(member);

            try (MockedStatic<TenantContext> mocked = mockStatic(TenantContext.class)) {
                mocked.when(TenantContext::getCurrentTenantId).thenReturn(orgB);

                ResponseEntity<ApiResponse<MemberResponse>> response =
                        memberController.getMemberByMembershipNumber("MEM-001");

                assertThat(response.getStatusCode().value()).isEqualTo(403);
            }
        }

        @Test
        @DisplayName("Should allow access when same organisation")
        void shouldAllowAccessWhenSameOrg() {
            Member member = buildMember(orgA);
            when(memberService.findByMembershipNumber("MEM-001")).thenReturn(member);

            try (MockedStatic<TenantContext> mocked = mockStatic(TenantContext.class)) {
                mocked.when(TenantContext::getCurrentTenantId).thenReturn(orgA);

                ResponseEntity<ApiResponse<MemberResponse>> response =
                        memberController.getMemberByMembershipNumber("MEM-001");

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().isSuccess()).isTrue();
            }
        }
    }

    @Nested
    @DisplayName("getMyProfile (GET /api/members/me)")
    class GetMyProfile {

        @Test
        @DisplayName("Should return the caller's own member profile")
        void shouldReturnCallersOwnProfile() {
            Member member = buildMember(orgA);
            UUID callerUserId = member.getUserId();
            when(memberService.findByUserId(callerUserId)).thenReturn(member);

            try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
                current.when(CurrentUser::requireUserId).thenReturn(callerUserId);

                ResponseEntity<ApiResponse<MemberResponse>> response = memberController.getMyProfile();

                assertThat(response.getStatusCode().value()).isEqualTo(200);
                assertThat(response.getBody()).isNotNull();
                assertThat(response.getBody().getData().id()).isEqualTo(memberId);
                assertThat(response.getBody().getData().userId()).isEqualTo(callerUserId);
            }
        }

        @Test
        @DisplayName("Should 404 when the caller has no member profile")
        void shouldNotFindWhenNoMemberProfile() {
            UUID callerUserId = UUID.randomUUID();
            when(memberService.findByUserId(callerUserId))
                    .thenThrow(new ResourceNotFoundException("Member", "userId=" + callerUserId));

            try (MockedStatic<CurrentUser> current = mockStatic(CurrentUser.class)) {
                current.when(CurrentUser::requireUserId).thenReturn(callerUserId);

                assertThatThrownBy(() -> memberController.getMyProfile())
                        .isInstanceOf(ResourceNotFoundException.class);
            }
        }
    }
}
