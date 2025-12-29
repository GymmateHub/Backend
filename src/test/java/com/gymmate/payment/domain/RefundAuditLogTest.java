package com.gymmate.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RefundAuditLog Entity Tests")
class RefundAuditLogTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create CREATED audit log")
        void created_Success() {
            // Arrange
            RefundRequestEntity request = createRequest(RefundRequestStatus.PENDING);
            UUID performedBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.created(request, performedBy, "MEMBER");

            // Assert
            assertThat(log.getRefundRequestId()).isEqualTo(request.getId());
            assertThat(log.getAction()).isEqualTo("CREATED");
            assertThat(log.getNewStatus()).isEqualTo("PENDING");
            assertThat(log.getPerformedByUserId()).isEqualTo(performedBy);
            assertThat(log.getPerformedByType()).isEqualTo("MEMBER");
            assertThat(log.getCreatedAt()).isNotNull();
        }

        @Test
        @DisplayName("Should create STATUS_CHANGED audit log")
        void statusChanged_Success() {
            // Arrange
            RefundRequestEntity request = createRequest(RefundRequestStatus.UNDER_REVIEW);
            UUID performedBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.statusChanged(
                    request, "PENDING", performedBy, "STAFF", "Started review");

            // Assert
            assertThat(log.getAction()).isEqualTo("STATUS_CHANGED");
            assertThat(log.getOldStatus()).isEqualTo("PENDING");
            assertThat(log.getNewStatus()).isEqualTo("UNDER_REVIEW");
            assertThat(log.getNotes()).isEqualTo("Started review");
        }

        @Test
        @DisplayName("Should create APPROVED audit log")
        void approved_Success() {
            // Arrange
            RefundRequestEntity request = createRequest(RefundRequestStatus.APPROVED);
            UUID approvedBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.approved(
                    request, approvedBy, "GYM_OWNER", "Good standing customer");

            // Assert
            assertThat(log.getAction()).isEqualTo("APPROVED");
            assertThat(log.getOldStatus()).isEqualTo("PENDING");
            assertThat(log.getNewStatus()).isEqualTo("APPROVED");
            assertThat(log.getNotes()).isEqualTo("Good standing customer");
            assertThat(log.getPerformedByUserId()).isEqualTo(approvedBy);
        }

        @Test
        @DisplayName("Should create REJECTED audit log")
        void rejected_Success() {
            // Arrange
            RefundRequestEntity request = createRequest(RefundRequestStatus.REJECTED);
            UUID rejectedBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.rejected(
                    request, rejectedBy, "GYM_OWNER", "Policy violation");

            // Assert
            assertThat(log.getAction()).isEqualTo("REJECTED");
            assertThat(log.getOldStatus()).isEqualTo("PENDING");
            assertThat(log.getNewStatus()).isEqualTo("REJECTED");
            assertThat(log.getNotes()).isEqualTo("Policy violation");
        }

        @Test
        @DisplayName("Should create PROCESSED audit log")
        void processed_Success() {
            // Arrange
            RefundRequestEntity request = createRequest(RefundRequestStatus.PROCESSED);
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test123")
                    .gymId(UUID.randomUUID())
                    .stripePaymentIntentId("pi_test")
                    .amount(new BigDecimal("50.00"))
                    .status(RefundStatus.SUCCEEDED)
                    .build();
            refund.setId(UUID.randomUUID());
            UUID processedBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.processed(
                    request, refund, processedBy, "SUPER_ADMIN");

            // Assert
            assertThat(log.getAction()).isEqualTo("PROCESSED");
            assertThat(log.getOldStatus()).isEqualTo("APPROVED");
            assertThat(log.getNewStatus()).isEqualTo("PROCESSED");
            assertThat(log.getPaymentRefundId()).isEqualTo(refund.getId());
            assertThat(log.getNotes()).contains("re_test123");
        }

        @Test
        @DisplayName("Should create ESCALATED audit log")
        void escalated_Success() {
            // Arrange
            RefundRequestEntity request = createRequest(RefundRequestStatus.PENDING);
            UUID escalatedBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.escalated(
                    request, escalatedBy, "GYM_OWNER", "SUPER_ADMIN");

            // Assert
            assertThat(log.getAction()).isEqualTo("ESCALATED");
            assertThat(log.getPerformedByUserId()).isEqualTo(escalatedBy);
            assertThat(log.getNotes()).contains("SUPER_ADMIN");
        }

        @Test
        @DisplayName("Should create CANCELLED audit log")
        void cancelled_Success() {
            // Arrange
            RefundRequestEntity request = createRequest(RefundRequestStatus.PENDING);
            UUID cancelledBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.cancelled(request, cancelledBy, "MEMBER");

            // Assert
            assertThat(log.getAction()).isEqualTo("CANCELLED");
            assertThat(log.getOldStatus()).isEqualTo("PENDING");
            assertThat(log.getNewStatus()).isEqualTo("CANCELLED");
            assertThat(log.getPerformedByUserId()).isEqualTo(cancelledBy);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build with all fields")
        void build_AllFields_Success() {
            // Arrange
            UUID requestId = UUID.randomUUID();
            UUID refundId = UUID.randomUUID();
            UUID performedBy = UUID.randomUUID();

            // Act
            RefundAuditLog log = RefundAuditLog.builder()
                    .refundRequestId(requestId)
                    .paymentRefundId(refundId)
                    .action("CUSTOM_ACTION")
                    .oldStatus("OLD")
                    .newStatus("NEW")
                    .performedByUserId(performedBy)
                    .performedByType("SYSTEM")
                    .notes("Custom notes")
                    .ipAddress("192.168.1.1")
                    .userAgent("Mozilla/5.0")
                    .metadata("{\"key\":\"value\"}")
                    .build();

            // Assert
            assertThat(log.getRefundRequestId()).isEqualTo(requestId);
            assertThat(log.getPaymentRefundId()).isEqualTo(refundId);
            assertThat(log.getAction()).isEqualTo("CUSTOM_ACTION");
            assertThat(log.getOldStatus()).isEqualTo("OLD");
            assertThat(log.getNewStatus()).isEqualTo("NEW");
            assertThat(log.getIpAddress()).isEqualTo("192.168.1.1");
            assertThat(log.getUserAgent()).isEqualTo("Mozilla/5.0");
            assertThat(log.getMetadata()).isEqualTo("{\"key\":\"value\"}");
        }

        @Test
        @DisplayName("Should set default createdAt")
        void build_DefaultCreatedAt() {
            // Act
            RefundAuditLog log = RefundAuditLog.builder()
                    .action("TEST")
                    .build();

            // Assert
            assertThat(log.getCreatedAt()).isNotNull();
        }
    }

    // Helper method
    private RefundRequestEntity createRequest(RefundRequestStatus status) {
        RefundRequestEntity request = RefundRequestEntity.builder()
                .gymId(UUID.randomUUID())
                .refundType(RefundType.MEMBER_PAYMENT)
                .originalPaymentAmount(new BigDecimal("100.00"))
                .requestedRefundAmount(new BigDecimal("50.00"))
                .requestedByUserId(UUID.randomUUID())
                .requestedByType("MEMBER")
                .refundToUserId(UUID.randomUUID())
                .refundToType("MEMBER")
                .reasonCategory(RefundReasonCategory.SERVICE_NOT_PROVIDED)
                .status(status)
                .build();
        request.setId(UUID.randomUUID());
        return request;
    }
}

