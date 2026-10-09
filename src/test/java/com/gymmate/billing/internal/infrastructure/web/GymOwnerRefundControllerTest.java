package com.gymmate.billing.internal.infrastructure.web;

import com.gymmate.billing.internal.domain.RefundAuditLog;
import com.gymmate.billing.internal.application.dto.RefundRequestResponse;
import com.gymmate.billing.internal.application.RefundRequestService;
import com.gymmate.shared.constants.RefundReasonCategory;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("GymOwnerRefundController Unit Tests")
class GymOwnerRefundControllerTest {

    @Mock
    private RefundRequestService refundRequestService;

    @InjectMocks
    private GymOwnerRefundController controller;

    private UUID gymId;
    private UUID requestId;
    private TenantAwareUserDetails gymOwnerUser;

    @BeforeEach
    void setUp() {
        gymId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        gymOwnerUser = new TenantAwareUserDetails(
                UUID.randomUUID(), gymId, "owner@gym.com", "password", "GYM_OWNER", true, true);
    }

    @Nested
    @DisplayName("getAllRefundRequests Tests")
    class GetAllRefundRequestsTests {

        @Test
        @DisplayName("Should return all refund requests")
        void getAllRefundRequests_ReturnsRequests() {
            try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
                // Arrange
                mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(gymId);
                RefundRequestResponse response = createRefundRequestResponse();
                when(refundRequestService.getAllRequests(gymId)).thenReturn(List.of(response));

                // Act
                ResponseEntity<ApiResponse<List<RefundRequestResponse>>> result = controller.getAllRefundRequests();

                // Assert
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody()).isNotNull();
                assertThat(result.getBody().isSuccess()).isTrue();
                assertThat(result.getBody().getData()).hasSize(1);
            }
        }

        @Test
        @DisplayName("Should return empty list when no requests")
        void getAllRefundRequests_NoRequests_ReturnsEmptyList() {
            try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
                // Arrange
                mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(gymId);
                when(refundRequestService.getAllRequests(gymId)).thenReturn(List.of());

                // Act
                ResponseEntity<ApiResponse<List<RefundRequestResponse>>> result = controller.getAllRefundRequests();

                // Assert
                assertThat(result.getBody().getData()).isEmpty();
            }
        }
    }

    @Nested
    @DisplayName("getPendingRefundRequests Tests")
    class GetPendingRefundRequestsTests {

        @Test
        @DisplayName("Should return pending requests")
        void getPendingRefundRequests_ReturnsOnlyPending() {
            try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
                // Arrange
                mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(gymId);
                RefundRequestResponse pendingRequest = createRefundRequestResponse(RefundRequestStatus.PENDING, null, null);
                when(refundRequestService.getPendingRequests(gymId)).thenReturn(List.of(pendingRequest));

                // Act
                ResponseEntity<ApiResponse<List<RefundRequestResponse>>> result = controller.getPendingRefundRequests();

                // Assert
                assertThat(result.getBody().getData()).hasSize(1);
                assertThat(result.getBody().getData().get(0).status()).isEqualTo(RefundRequestStatus.PENDING);
            }
        }
    }

    @Nested
    @DisplayName("approveRefundRequest Tests")
    class ApproveRefundRequestTests {

        @Test
        @DisplayName("Should approve refund request")
        void approveRefundRequest_Success() {
            // Arrange
            RefundRequestResponse approvedResponse = createRefundRequestResponse(RefundRequestStatus.APPROVED, null, null);

            when(refundRequestService.approveRequest(eq(requestId), any(), eq("GYM_OWNER"), anyString()))
                    .thenReturn(approvedResponse);

            // Act
            ResponseEntity<ApiResponse<RefundRequestResponse>> result =
                    controller.approveRefundRequest(requestId, "Approved", gymOwnerUser);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getMessage()).isEqualTo("Refund request approved");
        }
    }

    @Nested
    @DisplayName("rejectRefundRequest Tests")
    class RejectRefundRequestTests {

        @Test
        @DisplayName("Should reject refund request with reason")
        void rejectRefundRequest_Success() {
            // Arrange
            RefundRequestResponse rejectedResponse = createRefundRequestResponse(RefundRequestStatus.REJECTED, null, null);

            when(refundRequestService.rejectRequest(eq(requestId), any(), eq("GYM_OWNER"), anyString(), anyString()))
                    .thenReturn(rejectedResponse);

            // Act
            ResponseEntity<ApiResponse<RefundRequestResponse>> result =
                    controller.rejectRefundRequest(requestId, "Policy violation", "Customer violated terms", gymOwnerUser);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getMessage()).isEqualTo("Refund request rejected");
        }
    }

    @Nested
    @DisplayName("getRefundRequestAudit Tests")
    class GetAuditTrailTests {

        @Test
        @DisplayName("Should return audit trail")
        void getAuditTrail_ReturnsAuditLogs() {
            // Arrange
            RefundAuditLog log1 = RefundAuditLog.builder()
                    .refundRequestId(requestId)
                    .action("CREATED")
                    .createdAt(LocalDateTime.now().minusHours(2))
                    .build();
            RefundAuditLog log2 = RefundAuditLog.builder()
                    .refundRequestId(requestId)
                    .action("APPROVED")
                    .createdAt(LocalDateTime.now())
                    .build();

            when(refundRequestService.getAuditTrail(requestId)).thenReturn(List.of(log1, log2));

            // Act
            ResponseEntity<ApiResponse<List<RefundAuditLog>>> result =
                    controller.getRefundRequestAudit(requestId);

            // Assert
            assertThat(result.getBody().getData()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("escalateRefundRequest Tests")
    class EscalateRefundRequestTests {

        @Test
        @DisplayName("Should escalate refund request")
        void escalateRefundRequest_Success() {
            // Arrange
            RefundRequestResponse escalatedResponse =
                    createRefundRequestResponse(RefundRequestStatus.PENDING, true, "SUPER_ADMIN");

            when(refundRequestService.escalateRequest(eq(requestId), any(), eq("GYM_OWNER"), eq("SUPER_ADMIN")))
                    .thenReturn(escalatedResponse);

            // Act
            ResponseEntity<ApiResponse<RefundRequestResponse>> result =
                    controller.escalateRefundRequest(requestId, "SUPER_ADMIN", gymOwnerUser);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getMessage()).isEqualTo("Refund request escalated");
        }
    }

    // Helper method
    private RefundRequestResponse createRefundRequestResponse() {
        return createRefundRequestResponse(RefundRequestStatus.PENDING, null, null);
    }

    private RefundRequestResponse createRefundRequestResponse(RefundRequestStatus status, Boolean escalated, String escalatedTo) {
        return new RefundRequestResponse(
                requestId,
                gymId,
                RefundType.MEMBER_PAYMENT,
                "pi_test123",
                new BigDecimal("100.00"),
                new BigDecimal("50.00"),
                "USD",
                null,
                null,
                UUID.randomUUID(),
                "MEMBER",
                null,
                UUID.randomUUID(),
                "MEMBER",
                null,
                RefundReasonCategory.SERVICE_NOT_PROVIDED,
                null,
                status,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                escalated,
                escalatedTo,
                null,
                null,
                LocalDateTime.now(),
                null);
    }
}

