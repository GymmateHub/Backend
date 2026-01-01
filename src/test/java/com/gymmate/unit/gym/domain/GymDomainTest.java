package com.gymmate.unit.gym.domain;

import com.gymmate.gym.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Gym Domain Tests")
class GymDomainTest {

    @Nested
    @DisplayName("Gym Creation Tests")
    class GymCreationTests {

        @Test
        @DisplayName("Should create gym with constructor")
        void createGym_WithConstructor_Success() {
            // Arrange
            UUID organisationId = UUID.randomUUID();

            // Act
            Gym gym = new Gym(
                    "Test Gym",
                    "A great fitness center",
                    "contact@testgym.com",
                    "+1234567890",
                    organisationId
            );

            // Assert
            assertThat(gym.getName()).isEqualTo("Test Gym");
            assertThat(gym.getDescription()).isEqualTo("A great fitness center");
            assertThat(gym.getEmail()).isEqualTo("contact@testgym.com");
            assertThat(gym.getPhone()).isEqualTo("+1234567890");
            assertThat(gym.getOrganisationId()).isEqualTo(organisationId);
            assertThat(gym.getSlug()).isNotNull();
        }

        @Test
        @DisplayName("Should auto-generate slug from name")
        void createGym_GeneratesSlug_Success() {
            // Arrange & Act
            Gym gym = new Gym(
                    "Premium Fitness Center",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getSlug()).isNotNull();
            assertThat(gym.getSlug()).containsPattern("premium-fitness-center");
        }

        @Test
        @DisplayName("Should set default status to ACTIVE")
        void createGym_DefaultStatus_IsActive() {
            // Act
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getStatus()).isEqualTo(GymStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should set default subscription to starter")
        void createGym_DefaultSubscription_IsStarter() {
            // Act
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getSubscriptionPlan()).isEqualTo("starter");
        }
    }

    @Nested
    @DisplayName("GymStatus Tests")
    class GymStatusTests {

        @ParameterizedTest
        @EnumSource(GymStatus.class)
        @DisplayName("Should have all expected gym statuses")
        void allStatuses_ShouldExist(GymStatus status) {
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("Should have correct status values")
        void statusValues_ShouldBeCorrect() {
            assertThat(GymStatus.values())
                    .contains(
                            GymStatus.ACTIVE,
                            GymStatus.SUSPENDED,
                            GymStatus.CANCELLED
                    );
        }
    }

    @Nested
    @DisplayName("Gym Address Tests")
    class GymAddressTests {

        @Test
        @DisplayName("Should set address fields")
        void setAddressFields_ValidData_Success() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setAddress("123 Main St");
            gym.setCity("New York");
            gym.setState("NY");
            gym.setCountry("USA");
            gym.setPostalCode("10001");

            // Assert
            assertThat(gym.getAddress()).isEqualTo("123 Main St");
            assertThat(gym.getCity()).isEqualTo("New York");
            assertThat(gym.getState()).isEqualTo("NY");
            assertThat(gym.getCountry()).isEqualTo("USA");
            assertThat(gym.getPostalCode()).isEqualTo("10001");
        }
    }

    @Nested
    @DisplayName("Gym Settings Tests")
    class GymSettingsTests {

        @Test
        @DisplayName("Should set website and logo")
        void setWebsiteAndLogo_ValidData_Success() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setWebsite("https://testgym.com");
            gym.setLogoUrl("https://cdn.gymmate.com/logos/gym123.png");

            // Assert
            assertThat(gym.getWebsite()).isEqualTo("https://testgym.com");
            assertThat(gym.getLogoUrl()).isEqualTo("https://cdn.gymmate.com/logos/gym123.png");
        }

        @Test
        @DisplayName("Should set timezone")
        void setTimezone_ValidTimezone_Success() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setTimezone("America/New_York");

            // Assert
            assertThat(gym.getTimezone()).isEqualTo("America/New_York");
        }

        @Test
        @DisplayName("Should set currency")
        void setCurrency_ValidCurrency_Success() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setCurrency("EUR");

            // Assert
            assertThat(gym.getCurrency()).isEqualTo("EUR");
        }
    }

    @Nested
    @DisplayName("Gym Subscription Tests")
    class GymSubscriptionTests {

        @Test
        @DisplayName("Should upgrade subscription plan")
        void upgradeSubscription_ChangesPlan() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setSubscriptionPlan("professional");

            // Assert
            assertThat(gym.getSubscriptionPlan()).isEqualTo("professional");
        }

        @Test
        @DisplayName("Should set subscription expiration")
        void setSubscriptionExpiry_ValidDate_Success() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );
            LocalDateTime expiryDate = LocalDateTime.now().plusYears(1);

            // Act
            gym.setSubscriptionExpiresAt(expiryDate);

            // Assert
            assertThat(gym.getSubscriptionExpiresAt()).isEqualTo(expiryDate);
        }

        @Test
        @DisplayName("Should set max members")
        void setMaxMembers_ValidNumber_Success() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setMaxMembers(500);

            // Assert
            assertThat(gym.getMaxMembers()).isEqualTo(500);
        }
    }

    @Nested
    @DisplayName("Gym Status Transitions")
    class GymStatusTransitionsTests {

        @Test
        @DisplayName("Should suspend gym")
        void suspendGym_SetsStatusSuspended() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setStatus(GymStatus.SUSPENDED);

            // Assert
            assertThat(gym.getStatus()).isEqualTo(GymStatus.SUSPENDED);
        }

        @Test
        @DisplayName("Should reactivate suspended gym")
        void reactivateGym_SetsStatusActive() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );
            gym.setStatus(GymStatus.SUSPENDED);

            // Act
            gym.setStatus(GymStatus.ACTIVE);

            // Assert
            assertThat(gym.getStatus()).isEqualTo(GymStatus.ACTIVE);
        }
    }

    @Nested
    @DisplayName("Stripe Connect Tests")
    class StripeConnectTests {

        @Test
        @DisplayName("Should set Stripe Connect account ID")
        void setStripeConnectId_ValidId_Success() {
            // Arrange
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "email@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Act
            gym.setStripeConnectAccountId("acct_test123");

            // Assert
            assertThat(gym.getStripeConnectAccountId()).isEqualTo("acct_test123");
        }
    }
}
