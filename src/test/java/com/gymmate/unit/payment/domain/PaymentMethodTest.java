package com.gymmate.unit.payment.domain;

import com.gymmate.payment.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentMethod Entity Tests")
class PaymentMethodTest {

    @Nested
    @DisplayName("Factory Method Tests")
    class FactoryMethodTests {

        @Test
        @DisplayName("Should create organisation payment method")
        void forOrganisation_ValidData_Success() {
            // Arrange
            UUID organisationId = UUID.randomUUID();
            UUID gymId = UUID.randomUUID();

            // Act
            PaymentMethod method = PaymentMethod.forOrganisation(organisationId, gymId, "pm_test123",
                    PaymentMethodType.CARD);

            // Assert
            assertThat(method.getOwnerType()).isEqualTo(PaymentMethodOwnerType.ORGANISATION);
            assertThat(method.getOwnerId()).isEqualTo(organisationId);
            assertThat(method.getOrganisationId()).isEqualTo(organisationId);
            assertThat(method.getProviderPaymentMethodId()).isEqualTo("pm_test123");
            assertThat(method.getMethodType()).isEqualTo(PaymentMethodType.CARD);
            assertThat(method.isOrganisationPaymentMethod()).isTrue();
        }

        @Test
        @DisplayName("Should create member payment method")
        void forMember_ValidData_Success() {
            // Arrange
            UUID memberId = UUID.randomUUID();
            UUID gymId = UUID.randomUUID();

            // Act
            PaymentMethod method = PaymentMethod.forMember(memberId, gymId, "pm_test456", PaymentMethodType.CARD);

            // Assert
            assertThat(method.getOwnerType()).isEqualTo(PaymentMethodOwnerType.MEMBER);
            assertThat(method.getOwnerId()).isEqualTo(memberId);
            assertThat(method.getGymId()).isEqualTo(gymId);
            assertThat(method.getMemberId()).isEqualTo(memberId);
            assertThat(method.isOrganisationPaymentMethod()).isFalse();
            // assertThat(method.isMemberPaymentMethod()).isTrue(); // Removed
            // isMemberPaymentMethod check if method removed
        }
    }

    @Nested
    @DisplayName("Card Details Tests")
    class CardDetailsTests {

        @Test
        @DisplayName("Should store card details")
        void setCardDetails_ValidData_Success() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Act
            method.setCardBrand("visa");
            method.setCardLastFour("4242");
            method.setCardExpiresMonth(12);
            method.setCardExpiresYear(2025);

            // Assert
            assertThat(method.getCardBrand()).isEqualTo("visa");
            assertThat(method.getCardLastFour()).isEqualTo("4242");
            assertThat(method.getCardExpiresMonth()).isEqualTo(12);
            assertThat(method.getCardExpiresYear()).isEqualTo(2025);
        }

        @Test
        @DisplayName("Should identify card payment method")
        void isCard_CardType_ReturnsTrue() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Act & Assert
            assertThat(method.isCard()).isTrue();
            assertThat(method.isBankAccount()).isFalse();
        }
    }

    @Nested
    @DisplayName("Bank Account Details Tests")
    class BankAccountDetailsTests {

        @Test
        @DisplayName("Should store bank account details")
        void setBankDetails_ValidData_Success() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "ba_test",
                    PaymentMethodType.BANK_ACCOUNT);

            // Act
            method.setBankName("Chase");
            method.setBankLastFour("6789");

            // Assert
            assertThat(method.getBankName()).isEqualTo("Chase");
            assertThat(method.getBankLastFour()).isEqualTo("6789");
        }

        @Test
        @DisplayName("Should identify bank account payment method")
        void isBankAccount_BankType_ReturnsTrue() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "ba_test",
                    PaymentMethodType.BANK_ACCOUNT);

            // Act & Assert
            assertThat(method.isBankAccount()).isTrue();
            assertThat(method.isCard()).isFalse();
        }
    }

    @Nested
    @DisplayName("Status Tests")
    class StatusTests {

        @Test
        @DisplayName("Should set as default")
        void setAsDefault_ShouldUpdateFlag() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Act
            method.setAsDefault();

            // Assert
            assertThat(method.getIsDefault()).isTrue();
        }

        @Test
        @DisplayName("Should remove default")
        void removeDefault_ShouldClearFlag() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);
            method.setAsDefault();

            // Act
            method.removeDefault();

            // Assert
            assertThat(method.getIsDefault()).isFalse();
        }

        @Test
        @DisplayName("Should activate payment method")
        void activate_ShouldSetActive() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);
            method.deactivate();

            // Act
            method.activate();

            // Assert
            assertThat(method.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should deactivate payment method")
        void deactivate_ShouldClearActive() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Act
            method.deactivate();

            // Assert
            assertThat(method.getIsActive()).isFalse();
        }

        @Test
        @DisplayName("Should mark as verified")
        void markVerified_ShouldUpdateFlagAndTimestamp() {
            // Arrange
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Act
            method.markVerified();

            // Assert
            assertThat(method.getIsVerified()).isTrue();
            assertThat(method.getVerifiedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("PaymentMethodType Tests")
    class PaymentMethodTypeTests {

        @ParameterizedTest
        @EnumSource(PaymentMethodType.class)
        @DisplayName("Should accept all valid payment method types")
        void setMethodType_AllTypes_Success(PaymentMethodType type) {
            // Arrange
            PaymentMethod method = PaymentMethod.builder()
                    .ownerType(PaymentMethodOwnerType.ORGANISATION)
                    .ownerId(UUID.randomUUID())
                    .organisationId(UUID.randomUUID())
                    .gymId(UUID.randomUUID())
                    .providerPaymentMethodId("pm_test")
                    .methodType(type)
                    .build();

            // Assert
            assertThat(method.getMethodType()).isEqualTo(type);
        }

        @Test
        @DisplayName("Should have all expected method types")
        void allTypes_ShouldExist() {
            assertThat(PaymentMethodType.values())
                    .contains(
                            PaymentMethodType.CARD,
                            PaymentMethodType.BANK_ACCOUNT,
                            PaymentMethodType.DIGITAL_WALLET,
                            PaymentMethodType.OTHER);
        }
    }

    @Nested
    @DisplayName("PaymentMethodOwnerType Tests")
    class PaymentMethodOwnerTypeTests {

        @Test
        @DisplayName("Should have GYM and MEMBER types")
        void allTypes_ShouldExist() {
            assertThat(PaymentMethodOwnerType.values())
                    .contains(
                            PaymentMethodOwnerType.ORGANISATION,
                            PaymentMethodOwnerType.MEMBER);
        }
    }

    @Nested
    @DisplayName("Default Values Tests")
    class DefaultValuesTests {

        @Test
        @DisplayName("Should have default provider as stripe")
        void defaultProvider_ShouldBeStripe() {
            // Arrange & Act
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Assert
            assertThat(method.getProvider()).isEqualTo("stripe");
        }

        @Test
        @DisplayName("Should not be default by default")
        void defaultIsDefault_ShouldBeFalse() {
            // Arrange & Act
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Assert
            assertThat(method.getIsDefault()).isFalse();
        }

        @Test
        @DisplayName("Should be active by default")
        void defaultIsActive_ShouldBeTrue() {
            // Arrange & Act
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Assert - Uses inherited active field from BaseAuditEntity
            assertThat(method.getIsActive()).isTrue();
        }

        @Test
        @DisplayName("Should not be verified by default")
        void defaultIsVerified_ShouldBeFalse() {
            // Arrange & Act
            PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                    PaymentMethodType.CARD);

            // Assert
            assertThat(method.getIsVerified()).isFalse();
        }
    }
}
