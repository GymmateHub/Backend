package com.gymmate.payment.application;

import com.gymmate.payment.api.dto.CreateRefundRequestDTO;
import com.gymmate.payment.api.dto.RefundRequest;
import com.gymmate.payment.api.dto.RefundRequestResponse;
import com.gymmate.payment.api.dto.RefundResponse;
import com.gymmate.payment.domain.*;
import com.gymmate.payment.infrastructure.PaymentRefundRepository;
import com.gymmate.payment.infrastructure.RefundAuditLogRepository;
import com.gymmate.payment.infrastructure.RefundRequestRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.user.application.UserService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RefundRequestService Unit Tests")
class RefundRequestServiceTest {

        @Mock
        private RefundRequestRepository refundRequestRepository;

        @Mock
        private RefundAuditLogRepository auditLogRepository;

        @Mock
        private PaymentRefundRepository paymentRefundRepository;

        @Mock
        private StripePaymentService stripePaymentService;

        @Mock
        private UserService userService;

        private RefundRequestService refundRequestService;

        // Test data
        private UUID gymId;
        private UUID requesterId;
        private UUID recipientId;
        private String paymentIntentId;

        @BeforeEach
        void setUp() {
                refundRequestService = new RefundRequestService(
                                refundRequestRepository,
                                auditLogRepository,
                                paymentRefundRepository,
                                stripePaymentService,
                                userService);

                gymId = UUID.randomUUID();
                requesterId = UUID.randomUUID();
                recipientId = UUID.randomUUID();
                paymentIntentId = "pi_test123";
        }

        @Nested
        @DisplayName("Create Refund Request Tests")
        class CreateRefundRequestTests {

                @Test
                @DisplayName("Should create refund request successfully")
                void createRefundRequest_Success() {
                        // Arrange
                        CreateRefundRequestDTO dto = CreateRefundRequestDTO.builder()
                                        .refundType(RefundType.MEMBER_PAYMENT)
                                        .stripePaymentIntentId(paymentIntentId)
                                        .originalPaymentAmount(new BigDecimal("100.00"))
                                        .requestedRefundAmount(new BigDecimal("50.00"))
                                        .currency("USD")
                                        .reasonCategory(RefundReasonCategory.SERVICE_NOT_PROVIDED)
                                        .reasonDescription("Class was cancelled")
                                        .build();

                        when(refundRequestRepository.findByStripePaymentIntentIdAndStatus(
                                        paymentIntentId, RefundRequestStatus.PENDING))
                                        .thenReturn(Optional.empty());

                        when(refundRequestRepository.save(any(RefundRequestEntity.class)))
                                        .thenAnswer(invocation -> {
                                                RefundRequestEntity entity = invocation.getArgument(0);
                                                entity.setId(UUID.randomUUID());
                                                entity.setCreatedAt(LocalDateTime.now());
                                                entity.setUpdatedAt(LocalDateTime.now());
                                                return entity;
                                        });

                        when(auditLogRepository.save(any(RefundAuditLog.class)))
                                        .thenAnswer(invocation -> invocation.getArgument(0));

                        // Act
                        RefundRequestResponse response = refundRequestService.createRefundRequest(
                                        gymId, requesterId, "MEMBER", recipientId, "MEMBER", dto);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getGymId()).isEqualTo(gymId);
                        assertThat(response.getRefundType()).isEqualTo(RefundType.MEMBER_PAYMENT);
                        assertThat(response.getOriginalPaymentAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
                        assertThat(response.getRequestedRefundAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
                        assertThat(response.getStatus()).isEqualTo(RefundRequestStatus.PENDING);
                        assertThat(response.getReasonCategory()).isEqualTo(RefundReasonCategory.SERVICE_NOT_PROVIDED);

                        // Verify audit log was created
                        verify(auditLogRepository).save(any(RefundAuditLog.class));
                }

                @Test
                @DisplayName("Should reject when refund amount exceeds original payment")
                void createRefundRequest_AmountExceedsOriginal_ThrowsException() {
                        // Arrange
                        CreateRefundRequestDTO dto = CreateRefundRequestDTO.builder()
                                        .refundType(RefundType.MEMBER_PAYMENT)
                                        .stripePaymentIntentId(paymentIntentId)
                                        .originalPaymentAmount(new BigDecimal("50.00"))
                                        .requestedRefundAmount(new BigDecimal("100.00")) // More than original
                                        .reasonCategory(RefundReasonCategory.SERVICE_NOT_PROVIDED)
                                        .build();

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.createRefundRequest(
                                        gymId, requesterId, "MEMBER", recipientId, "MEMBER", dto))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "INVALID_REFUND_AMOUNT");

                        verify(refundRequestRepository, never()).save(any());
                }

                @Test
                @DisplayName("Should reject duplicate pending request for same payment")
                void createRefundRequest_DuplicatePending_ThrowsException() {
                        // Arrange
                        CreateRefundRequestDTO dto = CreateRefundRequestDTO.builder()
                                        .refundType(RefundType.MEMBER_PAYMENT)
                                        .stripePaymentIntentId(paymentIntentId)
                                        .originalPaymentAmount(new BigDecimal("100.00"))
                                        .requestedRefundAmount(new BigDecimal("50.00"))
                                        .reasonCategory(RefundReasonCategory.SERVICE_NOT_PROVIDED)
                                        .build();

                        RefundRequestEntity existingRequest = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PENDING)
                                        .build();

                        when(refundRequestRepository.findByStripePaymentIntentIdAndStatus(
                                        paymentIntentId, RefundRequestStatus.PENDING))
                                        .thenReturn(Optional.of(existingRequest));

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.createRefundRequest(
                                        gymId, requesterId, "MEMBER", recipientId, "MEMBER", dto))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "DUPLICATE_REFUND_REQUEST");

                        verify(refundRequestRepository, never()).save(any());
                }

                @Test
                @DisplayName("Should set default SLA due date")
                void createRefundRequest_SetsDueDate() {
                        // Arrange
                        CreateRefundRequestDTO dto = CreateRefundRequestDTO.builder()
                                        .refundType(RefundType.MEMBER_PAYMENT)
                                        .stripePaymentIntentId(paymentIntentId)
                                        .originalPaymentAmount(new BigDecimal("100.00"))
                                        .requestedRefundAmount(new BigDecimal("100.00"))
                                        .reasonCategory(RefundReasonCategory.SERVICE_NOT_PROVIDED)
                                        .build();

                        when(refundRequestRepository.findByStripePaymentIntentIdAndStatus(any(), any()))
                                        .thenReturn(Optional.empty());

                        ArgumentCaptor<RefundRequestEntity> captor = ArgumentCaptor.forClass(RefundRequestEntity.class);
                        when(refundRequestRepository.save(captor.capture()))
                                        .thenAnswer(invocation -> {
                                                RefundRequestEntity entity = invocation.getArgument(0);
                                                entity.setId(UUID.randomUUID());
                                                entity.setCreatedAt(LocalDateTime.now());
                                                return entity;
                                        });

                        // Act
                        refundRequestService.createRefundRequest(
                                        gymId, requesterId, "MEMBER", recipientId, "MEMBER", dto);

                        // Assert
                        RefundRequestEntity saved = captor.getValue();
                        assertThat(saved.getDueBy()).isNotNull();
                        assertThat(saved.getDueBy()).isAfter(LocalDateTime.now());
                        assertThat(saved.getDueBy()).isBefore(LocalDateTime.now().plusDays(4));
                }
        }

        @Nested
        @DisplayName("Approve Refund Request Tests")
        class ApproveRefundRequestTests {

                @Test
                @DisplayName("Should approve pending request successfully")
                void approveRequest_Success() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        UUID approverId = UUID.randomUUID();

                        RefundRequestEntity request = createPendingRequest(requestId);
                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
                        when(refundRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

                        // Act
                        RefundRequestResponse response = refundRequestService.approveRequest(
                                        requestId, approverId, "GYM_OWNER", "Approved for good customer");

                        // Assert
                        assertThat(response.getStatus()).isEqualTo(RefundRequestStatus.APPROVED);
                        assertThat(response.getProcessedByUserId()).isEqualTo(approverId);
                        verify(auditLogRepository).save(any(RefundAuditLog.class));
                }

                @Test
                @DisplayName("Should reject approval of already processed request")
                void approveRequest_AlreadyProcessed_ThrowsException() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PROCESSED)
                                        .build();
                        request.setId(requestId);

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.approveRequest(
                                        requestId, UUID.randomUUID(), "GYM_OWNER", null))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "CANNOT_APPROVE_REQUEST");
                }

                @Test
                @DisplayName("Should throw when request not found")
                void approveRequest_NotFound_ThrowsException() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.approveRequest(
                                        requestId, UUID.randomUUID(), "GYM_OWNER", null))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "REFUND_REQUEST_NOT_FOUND");
                }
        }

        @Nested
        @DisplayName("Reject Refund Request Tests")
        class RejectRefundRequestTests {

                @Test
                @DisplayName("Should reject pending request successfully")
                void rejectRequest_Success() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        UUID rejecterId = UUID.randomUUID();

                        RefundRequestEntity request = createPendingRequest(requestId);
                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
                        when(refundRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

                        // Act
                        RefundRequestResponse response = refundRequestService.rejectRequest(
                                        requestId, rejecterId, "GYM_OWNER", "Policy violation",
                                        "Customer misused service");

                        // Assert
                        assertThat(response.getStatus()).isEqualTo(RefundRequestStatus.REJECTED);
                        assertThat(response.getRejectionReason()).isEqualTo("Policy violation");
                        verify(auditLogRepository).save(any(RefundAuditLog.class));
                }

                @Test
                @DisplayName("Should not reject already rejected request")
                void rejectRequest_AlreadyRejected_ThrowsException() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.REJECTED)
                                        .build();
                        request.setId(requestId);

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.rejectRequest(
                                        requestId, UUID.randomUUID(), "GYM_OWNER", "reason", null))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "CANNOT_REJECT_REQUEST");
                }
        }

        @Nested
        @DisplayName("Cancel Refund Request Tests")
        class CancelRefundRequestTests {

                @Test
                @DisplayName("Should allow requester to cancel their own request")
                void cancelRequest_ByRequester_Success() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PENDING)
                                        .requestedByUserId(requesterId)
                                        .build();
                        request.setId(requestId);
                        request.setCreatedAt(LocalDateTime.now());

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
                        when(refundRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

                        // Act
                        RefundRequestResponse response = refundRequestService.cancelRequest(
                                        requestId, requesterId, "MEMBER");

                        // Assert
                        assertThat(response.getStatus()).isEqualTo(RefundRequestStatus.CANCELLED);
                        verify(auditLogRepository).save(any(RefundAuditLog.class));
                }

                @Test
                @DisplayName("Should allow admin to cancel any request")
                void cancelRequest_ByAdmin_Success() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        UUID adminId = UUID.randomUUID();
                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PENDING)
                                        .requestedByUserId(requesterId) // Different user
                                        .build();
                        request.setId(requestId);
                        request.setCreatedAt(LocalDateTime.now());

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
                        when(refundRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

                        // Act
                        RefundRequestResponse response = refundRequestService.cancelRequest(
                                        requestId, adminId, "SUPER_ADMIN");

                        // Assert
                        assertThat(response.getStatus()).isEqualTo(RefundRequestStatus.CANCELLED);
                }

                @Test
                @DisplayName("Should reject cancellation by non-requester non-admin")
                void cancelRequest_ByOtherUser_ThrowsException() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        UUID otherUserId = UUID.randomUUID();
                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PENDING)
                                        .requestedByUserId(requesterId)
                                        .build();
                        request.setId(requestId);

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.cancelRequest(
                                        requestId, otherUserId, "MEMBER"))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "CANCEL_NOT_ALLOWED");
                }

                @Test
                @DisplayName("Should not cancel already processed request")
                void cancelRequest_AlreadyProcessed_ThrowsException() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PROCESSED)
                                        .requestedByUserId(requesterId)
                                        .build();
                        request.setId(requestId);

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.cancelRequest(
                                        requestId, requesterId, "MEMBER"))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "CANNOT_CANCEL_REQUEST");
                }
        }

        @Nested
        @DisplayName("Process Approved Request Tests")
        class ProcessApprovedRequestTests {

                @Test
                @DisplayName("Should process approved request successfully")
                void processApprovedRequest_Success() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        UUID processorId = UUID.randomUUID();

                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .gymId(gymId)
                                        .status(RefundRequestStatus.APPROVED)
                                        .stripePaymentIntentId(paymentIntentId)
                                        .requestedRefundAmount(new BigDecimal("50.00"))
                                        .reasonDescription("Refund for cancelled class")
                                        .refundToUserId(recipientId)
                                        .refundToType("MEMBER")
                                        .build();
                        request.setId(requestId);
                        request.setCreatedAt(LocalDateTime.now());

                        RefundResponse stripeResponse = RefundResponse.builder()
                                        .refundId("re_test123")
                                        .paymentIntentId(paymentIntentId)
                                        .amount(new BigDecimal("50.00"))
                                        .currency("USD")
                                        .status("succeeded")
                                        .build();

                        PaymentRefund paymentRefund = PaymentRefund.builder()
                                        .stripeRefundId("re_test123")
                                        .build();
                        paymentRefund.setId(UUID.randomUUID());

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));
                        when(stripePaymentService.processRefund(eq(gymId), any(RefundRequest.class)))
                                        .thenReturn(stripeResponse);
                        when(paymentRefundRepository.findByStripeRefundId("re_test123"))
                                        .thenReturn(Optional.of(paymentRefund));
                        when(paymentRefundRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
                        when(refundRequestRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

                        // Act
                        RefundResponse response = refundRequestService.processApprovedRequest(
                                        requestId, processorId, "SUPER_ADMIN");

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getRefundId()).isEqualTo("re_test123");
                        assertThat(response.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));

                        // Verify payment refund was updated with tracking info
                        ArgumentCaptor<PaymentRefund> refundCaptor = ArgumentCaptor.forClass(PaymentRefund.class);
                        verify(paymentRefundRepository).save(refundCaptor.capture());
                        PaymentRefund savedRefund = refundCaptor.getValue();
                        assertThat(savedRefund.getRefundToUserId()).isEqualTo(recipientId);
                        assertThat(savedRefund.getProcessedByUserId()).isEqualTo(processorId);

                        verify(auditLogRepository).save(any(RefundAuditLog.class));
                }

                @Test
                @DisplayName("Should reject processing non-approved request")
                void processApprovedRequest_NotApproved_ThrowsException() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        RefundRequestEntity request = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PENDING) // Not approved
                                        .build();
                        request.setId(requestId);

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

                        // Act & Assert
                        assertThatThrownBy(() -> refundRequestService.processApprovedRequest(
                                        requestId, UUID.randomUUID(), "SUPER_ADMIN"))
                                        .isInstanceOf(DomainException.class)
                                        .hasFieldOrPropertyWithValue("errorCode", "REQUEST_NOT_APPROVED");

                        verify(stripePaymentService, never()).processRefund(any(), any());
                }
        }

        @Nested
        @DisplayName("Query Tests")
        class QueryTests {

                @Test
                @DisplayName("Should get pending requests for gym")
                void getPendingRequests_ReturnsList() {
                        // Arrange
                        RefundRequestEntity request1 = createPendingRequest(UUID.randomUUID());
                        RefundRequestEntity request2 = createPendingRequest(UUID.randomUUID());

                        when(refundRequestRepository.findPendingByGymId(gymId))
                                        .thenReturn(List.of(request1, request2));

                        // Act
                        List<RefundRequestResponse> responses = refundRequestService.getPendingRequests(gymId);

                        // Assert
                        assertThat(responses).hasSize(2);
                }

                @Test
                @DisplayName("Should get all requests for gym")
                void getAllRequests_ReturnsList() {
                        // Arrange
                        RefundRequestEntity pending = createPendingRequest(UUID.randomUUID());
                        RefundRequestEntity processed = RefundRequestEntity.builder()
                                        .status(RefundRequestStatus.PROCESSED)
                                        .gymId(gymId)
                                        .build();
                        processed.setId(UUID.randomUUID());
                        processed.setCreatedAt(LocalDateTime.now());

                        when(refundRequestRepository.findByGymIdOrderByCreatedAtDesc(gymId))
                                        .thenReturn(List.of(pending, processed));

                        // Act
                        List<RefundRequestResponse> responses = refundRequestService.getAllRequests(gymId);

                        // Assert
                        assertThat(responses).hasSize(2);
                }

                @Test
                @DisplayName("Should get single request by ID")
                void getRequest_ReturnsRequest() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        RefundRequestEntity request = createPendingRequest(requestId);

                        when(refundRequestRepository.findById(requestId)).thenReturn(Optional.of(request));

                        // Act
                        RefundRequestResponse response = refundRequestService.getRequest(requestId);

                        // Assert
                        assertThat(response).isNotNull();
                        assertThat(response.getId()).isEqualTo(requestId);
                }

                @Test
                @DisplayName("Should get audit trail for request")
                void getAuditTrail_ReturnsList() {
                        // Arrange
                        UUID requestId = UUID.randomUUID();
                        RefundAuditLog log1 = RefundAuditLog.builder()
                                        .refundRequestId(requestId)
                                        .action("CREATED")
                                        .createdAt(LocalDateTime.now().minusHours(2))
                                        .build();
                        RefundAuditLog log2 = RefundAuditLog.builder()
                                        .refundRequestId(requestId)
                                        .action("APPROVED")
                                        .createdAt(LocalDateTime.now().minusHours(1))
                                        .build();

                        when(auditLogRepository.findByRefundRequestIdOrderByCreatedAtAsc(requestId))
                                        .thenReturn(List.of(log1, log2));

                        // Act
                        List<RefundAuditLog> auditTrail = refundRequestService.getAuditTrail(requestId);

                        // Assert
                        assertThat(auditTrail).hasSize(2);
                        assertThat(auditTrail.get(0).getAction()).isEqualTo("CREATED");
                        assertThat(auditTrail.get(1).getAction()).isEqualTo("APPROVED");
                }
        }

        // Helper methods
        private RefundRequestEntity createPendingRequest(UUID requestId) {
                RefundRequestEntity request = RefundRequestEntity.builder()
                                .gymId(gymId)
                                .status(RefundRequestStatus.PENDING)
                                .refundType(RefundType.MEMBER_PAYMENT)
                                .stripePaymentIntentId(paymentIntentId)
                                .originalPaymentAmount(new BigDecimal("100.00"))
                                .requestedRefundAmount(new BigDecimal("50.00"))
                                .currency("USD")
                                .requestedByUserId(requesterId)
                                .requestedByType("MEMBER")
                                .refundToUserId(recipientId)
                                .refundToType("MEMBER")
                                .reasonCategory(RefundReasonCategory.SERVICE_NOT_PROVIDED)
                                .build();
                request.setId(requestId);
                request.setCreatedAt(LocalDateTime.now());
                request.setUpdatedAt(LocalDateTime.now());
                return request;
        }
}
