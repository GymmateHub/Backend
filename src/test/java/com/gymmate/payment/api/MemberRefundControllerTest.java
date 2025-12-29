package com.gymmate.payment.api;

import com.gymmate.payment.api.dto.CreateRefundRequestDTO;
import com.gymmate.payment.api.dto.RefundRequestResponse;
import com.gymmate.payment.application.RefundRequestService;
import com.gymmate.payment.domain.*;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemberRefundController Unit Tests")
class MemberRefundControllerTest {

    @Mock
    private RefundRequestService refundRequestService;

    @InjectMocks
    private MemberRefundController controller;

    private UUID gymId;
    private UUID memberId;
    private UUID requestId;
    private TenantAwareUserDetails memberUser;

    @BeforeEach
    void setUp() {
        gymId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        requestId = UUID.randomUUID();
        memberUser = new TenantAwareUserDetails(
                memberId, gymId, "member@gym.com", "password", "MEMBER", true, true);
    }

    @Nested
    @DisplayName("requestRefund Tests")
    class RequestRefundTests {

        @Test
        @DisplayName("Should create refund request for member")
        void requestRefund_Success() {
            try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
                // Arrange
                mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(gymId);

                CreateRefundRequestDTO request = CreateRefundRequestDTO.builder()
                        .refundType(RefundType.MEMBER_PAYMENT)
                        .stripePaymentIntentId("pi_test123")
                        .originalPaymentAmount(new BigDecimal("100.00"))
                        .requestedRefundAmount(new BigDecimal("100.00"))
                        .reasonCategory(RefundReasonCategory.CLASS_CANCELLED)
                        .reasonDescription("Trainer was sick, class cancelled")
                        .build();

                RefundRequestResponse response = createRefundRequestResponse();
                when(refundRequestService.createRefundRequest(any(), any(), anyString(), any(), anyString(), any()))
                        .thenReturn(response);

                // Act
                ResponseEntity<ApiResponse<RefundRequestResponse>> result =
                        controller.requestRefund(request, memberUser);

                // Assert
                assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
                assertThat(result.getBody()).isNotNull();
                assertThat(result.getBody().isSuccess()).isTrue();
                assertThat(result.getBody().getMessage()).isEqualTo("Refund request submitted successfully");
            }
        }

        @Test
        @DisplayName("Should force MEMBER_PAYMENT type for member requests")
        void requestRefund_ForceMemberPaymentType() {
            try (MockedStatic<TenantContext> mockedTenantContext = mockStatic(TenantContext.class)) {
                // Arrange
                mockedTenantContext.when(TenantContext::getCurrentTenantId).thenReturn(gymId);

                CreateRefundRequestDTO request = CreateRefundRequestDTO.builder()
                        .refundType(RefundType.PLATFORM_SUBSCRIPTION) // Try to use wrong type
                        .stripePaymentIntentId("pi_test123")
                        .originalPaymentAmount(new BigDecimal("100.00"))
                        .requestedRefundAmount(new BigDecimal("100.00"))
                        .reasonCategory(RefundReasonCategory.CLASS_CANCELLED)
                        .build();

                RefundRequestResponse response = createRefundRequestResponse();
                when(refundRequestService.createRefundRequest(any(), any(), anyString(), any(), anyString(), any()))
                        .thenReturn(response);

                // Act
                controller.requestRefund(request, memberUser);

                // Assert - type should be changed to MEMBER_PAYMENT
                assertThat(request.getRefundType()).isEqualTo(RefundType.MEMBER_PAYMENT);
            }
        }
    }

    @Nested
    @DisplayName("getMyRefundRequests Tests")
    class GetMyRequestsTests {

        @Test
        @DisplayName("Should return member's own refund requests")
        void getMyRequests_ReturnsOwnRequests() {
            // Arrange
            RefundRequestResponse response = createRefundRequestResponse();
            when(refundRequestService.getMyRequests(memberId)).thenReturn(List.of(response));

            // Act
            ResponseEntity<ApiResponse<List<RefundRequestResponse>>> result =
                    controller.getMyRefundRequests(memberUser);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getData()).hasSize(1);
        }

        @Test
        @DisplayName("Should return empty list if no requests")
        void getMyRequests_NoRequests_ReturnsEmptyList() {
            // Arrange
            when(refundRequestService.getMyRequests(memberId)).thenReturn(List.of());

            // Act
            ResponseEntity<ApiResponse<List<RefundRequestResponse>>> result =
                    controller.getMyRefundRequests(memberUser);

            // Assert
            assertThat(result.getBody().getData()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getMyRefundRequest Tests")
    class GetMyRequestByIdTests {

        @Test
        @DisplayName("Should return request if owned by member")
        void getMyRequest_OwnedByMember_ReturnsRequest() {
            // Arrange
            RefundRequestResponse response = createRefundRequestResponse();
            response.setRequestedByUserId(memberId); // Same as current user
            when(refundRequestService.getRequest(requestId)).thenReturn(response);

            // Act
            ResponseEntity<ApiResponse<RefundRequestResponse>> result =
                    controller.getMyRefundRequest(requestId, memberUser);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getData().getId()).isEqualTo(requestId);
        }

        @Test
        @DisplayName("Should throw exception for other member's request")
        void getMyRequest_NotOwnedByMember_ThrowsException() {
            // Arrange
            RefundRequestResponse response = createRefundRequestResponse();
            response.setRequestedByUserId(UUID.randomUUID()); // Different user
            when(refundRequestService.getRequest(requestId)).thenReturn(response);

            // Act & Assert
            assertThatThrownBy(() -> controller.getMyRefundRequest(requestId, memberUser))
                    .isInstanceOf(DomainException.class)
                    .hasFieldOrPropertyWithValue("errorCode", "ACCESS_DENIED");
        }
    }

    @Nested
    @DisplayName("cancelMyRefundRequest Tests")
    class CancelMyRequestTests {

        @Test
        @DisplayName("Should cancel pending request")
        void cancelMyRequest_Success() {
            // Arrange
            RefundRequestResponse cancelledResponse = createRefundRequestResponse();
            cancelledResponse.setStatus(RefundRequestStatus.CANCELLED);
            when(refundRequestService.cancelRequest(requestId, memberId, "MEMBER"))
                    .thenReturn(cancelledResponse);

            // Act
            ResponseEntity<ApiResponse<RefundRequestResponse>> result =
                    controller.cancelMyRefundRequest(requestId, memberUser);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getMessage()).isEqualTo("Refund request cancelled");
        }
    }

    // Helper method
    private RefundRequestResponse createRefundRequestResponse() {
        return RefundRequestResponse.builder()
                .id(requestId)
                .gymId(gymId)
                .refundType(RefundType.MEMBER_PAYMENT)
                .stripePaymentIntentId("pi_test123")
                .originalPaymentAmount(new BigDecimal("100.00"))
                .requestedRefundAmount(new BigDecimal("100.00"))
                .currency("USD")
                .requestedByUserId(memberId)
                .requestedByType("MEMBER")
                .refundToUserId(memberId)
                .refundToType("MEMBER")
                .reasonCategory(RefundReasonCategory.CLASS_CANCELLED)
                .status(RefundRequestStatus.PENDING)
                .createdAt(LocalDateTime.now())
                .build();
    }
}

