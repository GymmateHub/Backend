package com.gymmate.unit.gym.domain;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.domain.GymStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Gym Entity Tests")
class GymTest {

    @Nested
    @DisplayName("Gym Creation Tests")
    class GymCreationTests {

        @Test
        @DisplayName("Should create gym with valid data")
        void createGym_WithValidData_Success() {
            // Arrange
            UUID organisationId = UUID.randomUUID();

            // Act
            Gym gym = new Gym(
                    "Fitness World",
                    "A premium fitness center",
                    "contact@fitnessworld.com",
                    "+1234567890",
                    organisationId
            );

            // Assert
            assertThat(gym.getName()).isEqualTo("Fitness World");
            assertThat(gym.getDescription()).isEqualTo("A premium fitness center");
            assertThat(gym.getContactEmail()).isEqualTo("contact@fitnessworld.com");
            assertThat(gym.getContactPhone()).isEqualTo("+1234567890");
            assertThat(gym.getOrganisationId()).isEqualTo(organisationId);
        }

        @Test
        @DisplayName("Should generate slug from name")
        void createGym_ShouldGenerateSlug() {
            // Arrange & Act
            Gym gym = new Gym(
                    "Fitness World",
                    "Description",
                    "contact@test.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert - slug now includes timestamp suffix for uniqueness
            assertThat(gym.getSlug()).isNotNull();
            assertThat(gym.getSlug()).startsWith("fitness-world-");
        }

        @Test
        @DisplayName("Should have default status as ACTIVE")
        void createGym_DefaultStatus_IsActive() {
            // Arrange & Act
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "test@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getStatus()).isEqualTo(GymStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should have default subscription plan as starter")
        void createGym_DefaultSubscriptionPlan_IsStarter() {
            // Arrange & Act
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "test@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getSubscriptionPlan()).isEqualTo("starter");
        }

        @Test
        @DisplayName("Should have default timezone as UTC")
        void createGym_DefaultTimezone_IsUTC() {
            // Arrange & Act
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "test@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getTimezone()).isEqualTo("UTC");
        }

        @Test
        @DisplayName("Should have default currency as USD")
        void createGym_DefaultCurrency_IsUSD() {
            // Arrange & Act
            Gym gym = new Gym(
                    "Test Gym",
                    "Description",
                    "test@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getCurrency()).isEqualTo("USD");
        }
    }

    @Nested
    @DisplayName("Gym Status Tests")
    class GymStatusTests {

        @ParameterizedTest
        @EnumSource(GymStatus.class)
        @DisplayName("Should accept all valid gym statuses")
        void setStatus_WithValidStatus_Success(GymStatus status) {
            // Arrange
            Gym gym = createTestGym();

            // Act
            gym.setStatus(status);

            // Assert
            assertThat(gym.getStatus()).isEqualTo(status);
        }

        @Test
        @DisplayName("Should allow suspension")
        void statusTransition_ActiveToSuspended_Success() {
            // Arrange
            Gym gym = createTestGym();

            // Act
            gym.setStatus(GymStatus.SUSPENDED);

            // Assert
            assertThat(gym.getStatus()).isEqualTo(GymStatus.SUSPENDED);
        }
    }

    @Nested
    @DisplayName("Gym Update Tests")
    class GymUpdateTests {

        @Test
        @DisplayName("Should update gym details")
        void updateDetails_ValidData_Success() {
            // Arrange
            Gym gym = createTestGym();

            // Act
            gym.updateDetails(
                    "New Gym Name",
                    "New Description",
                    "new@email.com",
                    "+9876543210",
                    "https://newwebsite.com"
            );

            // Assert
            assertThat(gym.getName()).isEqualTo("New Gym Name");
            assertThat(gym.getDescription()).isEqualTo("New Description");
            assertThat(gym.getContactEmail()).isEqualTo("new@email.com");
            assertThat(gym.getContactPhone()).isEqualTo("+9876543210");
            assertThat(gym.getWebsite()).isEqualTo("https://newwebsite.com");
        }

        @Test
        @DisplayName("Should update address")
        void updateAddress_ValidData_Success() {
            // Arrange
            Gym gym = createTestGym();

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
    @DisplayName("Stripe Connect Tests")
    class StripeConnectTests {

        @Test
        @DisplayName("Should set Stripe Connect account ID")
        void setStripeConnectAccountId_ValidId_Success() {
            // Arrange
            Gym gym = createTestGym();

            // Act
            gym.setStripeConnectAccountId("acct_1234567890");

            // Assert
            assertThat(gym.getStripeConnectAccountId()).isEqualTo("acct_1234567890");
        }

        @Test
        @DisplayName("Should track Stripe onboarding status")
        void stripeOnboarding_TrackStatus() {
            // Arrange
            Gym gym = createTestGym();

            // Act
            gym.setStripeChargesEnabled(true);
            gym.setStripePayoutsEnabled(true);
            gym.setStripeDetailsSubmitted(true);

            // Assert
            assertThat(gym.getStripeChargesEnabled()).isTrue();
            assertThat(gym.getStripePayoutsEnabled()).isTrue();
            assertThat(gym.getStripeDetailsSubmitted()).isTrue();
        }

        @Test
        @DisplayName("Should have Stripe flags false by default")
        void stripeFlags_DefaultValues_AreFalse() {
            // Arrange & Act
            Gym gym = createTestGym();

            // Assert
            assertThat(gym.getStripeChargesEnabled()).isFalse();
            assertThat(gym.getStripePayoutsEnabled()).isFalse();
            assertThat(gym.getStripeDetailsSubmitted()).isFalse();
        }
    }

    @Nested
    @DisplayName("Slug Generation Tests")
    class SlugGenerationTests {

        @Test
        @DisplayName("Should convert name to lowercase slug")
        void generateSlug_UppercaseName_LowercaseSlug() {
            // Arrange & Act
            Gym gym = new Gym(
                    "FITNESS WORLD",
                    "Description",
                    "test@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert - slug now includes timestamp suffix for uniqueness
            assertThat(gym.getSlug()).startsWith("fitness-world-");
        }

        @Test
        @DisplayName("Should replace spaces with hyphens")
        void generateSlug_SpacesInName_ReplacedWithHyphens() {
            // Arrange & Act
            Gym gym = new Gym(
                    "My Awesome Gym",
                    "Description",
                    "test@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert - slug now includes timestamp suffix for uniqueness
            assertThat(gym.getSlug()).startsWith("my-awesome-gym-");
        }

        @Test
        @DisplayName("Should remove special characters")
        void generateSlug_SpecialCharacters_Removed() {
            // Arrange & Act
            Gym gym = new Gym(
                    "Gym's & Fitness!",
                    "Description",
                    "test@gym.com",
                    "+1234567890",
                    UUID.randomUUID()
            );

            // Assert
            assertThat(gym.getSlug()).doesNotContain("'", "&", "!");
        }
    }

    @Nested
    @DisplayName("Onboarding Tests")
    class OnboardingTests {

        @Test
        @DisplayName("Should track onboarding completion")
        void onboardingCompleted_SetTrue_Success() {
            // Arrange
            Gym gym = createTestGym();

            // Act
            gym.setOnboardingCompleted(true);

            // Assert
            assertThat(gym.isOnboardingCompleted()).isTrue();
        }

        @Test
        @DisplayName("Should have onboarding not completed by default")
        void onboardingCompleted_DefaultValue_IsFalse() {
            // Arrange & Act
            Gym gym = createTestGym();

            // Assert
            assertThat(gym.isOnboardingCompleted()).isFalse();
        }
    }

    private Gym createTestGym() {
        return new Gym(
                "Test Gym",
                "Test Description",
                "test@gym.com",
                "+1234567890",
                UUID.randomUUID()
        );
    }
}

