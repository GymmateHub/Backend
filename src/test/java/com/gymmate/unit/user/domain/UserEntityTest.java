package com.gymmate.unit.user.domain;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.shared.domain.TenantEntity;
import com.gymmate.user.domain.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for the User entity.
 * Validates that User properly extends TenantEntity, inherits
 * organisationId, and supports tenant validation.
 */
@DisplayName("User Entity Tests")
class UserEntityTest {

    @Nested
    @DisplayName("TenantEntity Inheritance")
    class TenantEntityInheritance {

        @Test
        @DisplayName("User should extend TenantEntity")
        void userShouldExtendTenantEntity() {
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            assertThat(user).isInstanceOf(TenantEntity.class);
        }

        @Test
        @DisplayName("User should inherit organisationId from TenantEntity")
        void userShouldInheritOrganisationId() {
            UUID orgId = UUID.randomUUID();
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            user.setOrganisationId(orgId);

            assertThat(user.getOrganisationId()).isEqualTo(orgId);
        }

        @Test
        @DisplayName("User with null organisationId should be allowed (e.g., SUPER_ADMIN)")
        void userShouldAllowNullOrganisationId() {
            User user = User.builder()
                    .email("admin@gymmate.com")
                    .role(UserRole.SUPER_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();

            assertThat(user.getOrganisationId()).isNull();
        }
    }

    @Nested
    @DisplayName("Tenant Validation")
    class TenantValidation {

        @Test
        @DisplayName("validateTenant should pass when organisationId matches")
        void shouldPassWhenOrganisationMatches() {
            UUID orgId = UUID.randomUUID();
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            user.setOrganisationId(orgId);

            // Should not throw
            user.validateTenant(orgId);
        }

        @Test
        @DisplayName("validateTenant should throw when organisationId does not match")
        void shouldThrowWhenOrganisationDoesNotMatch() {
            UUID orgA = UUID.randomUUID();
            UUID orgB = UUID.randomUUID();
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();
            user.setOrganisationId(orgA);

            assertThatThrownBy(() -> user.validateTenant(orgB))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("different organisation");
        }

        @Test
        @DisplayName("validateTenant should pass when user has null organisationId")
        void shouldPassWhenUserHasNullOrganisationId() {
            UUID orgId = UUID.randomUUID();
            User user = User.builder()
                    .email("admin@gymmate.com")
                    .role(UserRole.SUPER_ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            // organisationId is null

            // Should not throw — null org means no restriction
            user.validateTenant(orgId);
        }
    }

    @Nested
    @DisplayName("Login Attempts & Account Locking")
    class LoginAttempts {

        @Test
        @DisplayName("Should lock account after 5 failed attempts for 1 hour")
        void shouldLockAfterMaxAttempts() {
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            for (int i = 0; i < 5; i++) {
                user.incrementLoginAttempts();
            }

            assertThat(user.getLoginAttempts()).isEqualTo(5);
            assertThat(user.getLockedUntil()).isNotNull();
            // isActive checks lockedUntil — user is locked
            assertThat(user.isActive()).isFalse();
        }

        @Test
        @DisplayName("Should remain active before max attempts")
        void shouldRemainActiveBeforeMaxAttempts() {
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            for (int i = 0; i < 4; i++) {
                user.incrementLoginAttempts();
            }

            assertThat(user.getLoginAttempts()).isEqualTo(4);
            assertThat(user.getLockedUntil()).isNull();
            assertThat(user.isActive()).isTrue();
        }

        @Test
        @DisplayName("resetLoginAttempts should clear attempts and lock")
        void shouldResetLoginAttempts() {
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            // Lock the user
            for (int i = 0; i < 5; i++) {
                user.incrementLoginAttempts();
            }
            assertThat(user.isActive()).isFalse();

            // Reset
            user.resetLoginAttempts();
            assertThat(user.getLoginAttempts()).isZero();
            assertThat(user.getLockedUntil()).isNull();
            assertThat(user.isActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("Profile Operations")
    class ProfileOperations {

        @Test
        @DisplayName("updateProfile should update non-null fields only")
        void shouldUpdateNonNullFieldsOnly() {
            User user = User.builder()
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .phone("+1234567890")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            user.updateProfile("Jane", null, null);

            assertThat(user.getFirstName()).isEqualTo("Jane");
            assertThat(user.getLastName()).isEqualTo("Doe"); // unchanged
            assertThat(user.getPhone()).isEqualTo("+1234567890"); // unchanged
        }

        @Test
        @DisplayName("getFullName should return email when name is not set")
        void shouldReturnEmailWhenNameNotSet() {
            User user = User.builder()
                    .email("test@example.com")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            assertThat(user.getFullName()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("getFullName should return first + last name when set")
        void shouldReturnFullName() {
            User user = User.builder()
                    .email("test@example.com")
                    .firstName("John")
                    .lastName("Doe")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            assertThat(user.getFullName()).isEqualTo("John Doe");
        }

        @Test
        @DisplayName("verifyEmail should set verified and clear token")
        void shouldVerifyEmail() {
            User user = User.builder()
                    .email("test@example.com")
                    .emailVerified(false)
                    .emailVerificationToken("some-token")
                    .role(UserRole.MEMBER)
                    .status(UserStatus.ACTIVE)
                    .build();

            user.verifyEmail();

            assertThat(user.isEmailVerified()).isTrue();
            assertThat(user.getEmailVerificationToken()).isNull();
        }
    }
}

