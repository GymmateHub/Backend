package com.gymmate.billing.internal.infrastructure.web;

import com.gymmate.billing.internal.application.dto.CreateRefundRequestDTO;
import com.gymmate.billing.internal.application.dto.RefundRequestResponse;
import com.gymmate.billing.internal.application.RefundRequestService;
import com.gymmate.shared.constants.RefundReasonCategory;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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

                CreateRefundRequestDTO request = new CreateRefundRequestDTO(
                        RefundType.MEMBER_PAYMENT,
                        "pi_test123",
                        null,
                        new BigDecimal("100.00"),
                        new BigDecimal("100.00"),
                        null,
                        null,
                        null,
                        RefundReasonCategory.CLASS_CANCELLED,
                        "Trainer was sick, class cancelled",
                        null);

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

                CreateRefundRequestDTO request = new CreateRefundRequestDTO(
                        RefundType.PLATFORM_SUBSCRIPTION, // Try to use wrong type
                        "pi_test123",
                        null,
                        new BigDecimal("100.00"),
                        new BigDecimal("100.00"),
                        null,
                        null,
                        null,
                        RefundReasonCategory.CLASS_CANCELLED,
                        null,
                        null);

                RefundRequestResponse response = createRefundRequestResponse();
                when(refundRequestService.createRefundRequest(any(), any(), anyString(), any(), anyString(), any()))
                        .thenReturn(response);

                // Act
                controller.requestRefund(request, memberUser);

                // Assert - the service receives the request with its type changed to MEMBER_PAYMENT
                ArgumentCaptor<CreateRefundRequestDTO> sent = ArgumentCaptor.forClass(CreateRefundRequestDTO.class);
                verify(refundRequestService).createRefundRequest(any(), any(), anyString(), any(), anyString(), sent.capture());
                assertThat(sent.getValue().refundType()).isEqualTo(RefundType.MEMBER_PAYMENT);
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
            RefundRequestResponse response =
                    createRefundRequestResponse(memberId, RefundRequestStatus.PENDING); // Same as current user
            when(refundRequestService.getRequest(requestId)).thenReturn(response);

            // Act
            ResponseEntity<ApiResponse<RefundRequestResponse>> result =
                    controller.getMyRefundRequest(requestId, memberUser);

            // Assert
            assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(result.getBody().getData().id()).isEqualTo(requestId);
        }

        @Test
        @DisplayName("Should throw exception for other member's request")
        void getMyRequest_NotOwnedByMember_ThrowsException() {
            // Arrange
            RefundRequestResponse response =
                    createRefundRequestResponse(UUID.randomUUID(), RefundRequestStatus.PENDING); // Different user
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
            RefundRequestResponse cancelledResponse = createRefundRequestResponse(memberId, RefundRequestStatus.CANCELLED);
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
        return createRefundRequestResponse(memberId, RefundRequestStatus.PENDING);
    }

    private RefundRequestResponse createRefundRequestResponse(UUID requestedByUserId, RefundRequestStatus status) {
        return new RefundRequestResponse(
                requestId,
                gymId,
                RefundType.MEMBER_PAYMENT,
                "pi_test123",
                new BigDecimal("100.00"),
                new BigDecimal("100.00"),
                "USD",
                null,
                null,
                requestedByUserId,
                "MEMBER",
                null,
                memberId,
                "MEMBER",
                null,
                RefundReasonCategory.CLASS_CANCELLED,
                null,
                status,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                LocalDateTime.now(),
                null);
    }
}

