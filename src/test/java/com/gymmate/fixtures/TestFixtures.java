package com.gymmate.fixtures;

import com.gymmate.gym.domain.Gym;
import com.gymmate.payment.domain.*;
import com.gymmate.user.domain.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Test fixtures factory for creating test data objects.
 * Provides consistent, valid test data for use across all test classes.
 */
public class TestFixtures {

    // ==================== USER FIXTURES ====================

    public static User createUser() {
        return createUser("test@example.com", UserRole.MEMBER);
    }

    public static User createUser(String email, UserRole role) {
        return User.builder()
                .email(email)
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Test")
                .lastName("User")
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
    }

    public static User createGymOwner() {
        return createUser("owner@gym.com", UserRole.OWNER);
    }

    public static User createSuperAdmin() {
        return createUser("admin@gymmate.com", UserRole.SUPER_ADMIN);
    }

    public static User createStaff(UUID gymId) {
        User user = User.builder()
                .email("staff@gym.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Staff")
                .lastName("User")
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        user.setOrganisationId(gymId);
        return user;
    }

    public static User createTrainer(UUID gymId) {
        User user = User.builder()
                .email("trainer@gym.com")
                .passwordHash("$2a$10$hashedpassword")
                .firstName("Trainer")
                .lastName("User")
                .role(UserRole.TRAINER)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        user.setOrganisationId(gymId);
        return user;
    }

    public static Member createMember(UUID userId) {
        return Member.builder()
                .userId(userId)
                .membershipNumber("MEM-" + System.currentTimeMillis())
                .joinDate(LocalDate.now())
                .status(MemberStatus.ACTIVE)
                .build();
    }

    // ==================== GYM FIXTURES ====================

    public static Gym createGym() {
        return createGym("Test Gym", UUID.randomUUID());
    }

    public static Gym createGym(String name, UUID ownerId) {
        Gym gym = new Gym(
                name,
                "Description for " + name,
                "contact@" + name.toLowerCase().replace(" ", "") + ".com",
                "+1234567890",
                ownerId);
        gym.setCity("Test City");
        gym.setState("TS");
        gym.setCountry("USA");
        gym.setPostalCode("12345");
        return gym;
    }

    public static Gym createGymWithStripe(UUID ownerId) {
        Gym gym = createGym("Stripe Connected Gym", ownerId);
        gym.setStripeConnectAccountId("acct_test" + System.currentTimeMillis());
        gym.setStripeChargesEnabled(true);
        gym.setStripePayoutsEnabled(true);
        gym.setStripeDetailsSubmitted(true);
        return gym;
    }

    // ==================== PAYMENT FIXTURES ====================

    public static PaymentMethod createOrganisationPaymentMethod(UUID organisationId) {
        PaymentMethod method = PaymentMethod.forOrganisation(organisationId, null,
                "pm_test" + System.currentTimeMillis(), PaymentMethodType.CARD);
        method.setId(UUID.randomUUID());
        method.setCardBrand("visa");
        method.setCardLastFour("4242");
        method.setCardExpiresMonth(12);
        method.setCardExpiresYear(2025);
        return method;
    }

    public static PaymentMethod createMemberPaymentMethod(UUID memberId, UUID gymId) {
        PaymentMethod method = PaymentMethod.forMember(memberId, gymId, "pm_member" + System.currentTimeMillis(),
                PaymentMethodType.CARD);
        method.setId(UUID.randomUUID());
        method.setCardBrand("mastercard");
        method.setCardLastFour("5555");
        method.setCardExpiresMonth(6);
        method.setCardExpiresYear(2026);
        return method;
    }

    public static PaymentRefund createRefund(UUID gymId, BigDecimal amount) {
        // Refund is still gym-specific for Member Payments, but let's check
        // PaymentRefund entity if it uses gymId or organisationId
        // Assuming PaymentRefund uses GymId for member payments refund. Let's keep
        // gymId here if PaymentRefund entity wasn't changed.
        // But previously I saw PaymentRefund has both.
        PaymentRefund refund = PaymentRefund.builder()
                .gymId(gymId)
                .stripeRefundId("re_test" + System.currentTimeMillis())
                .stripePaymentIntentId("pi_test" + System.currentTimeMillis())
                .amount(amount)
                .currency("usd")
                .status(RefundStatus.PENDING)
                .refundType(RefundType.MEMBER_PAYMENT)
                .build();
        refund.setId(UUID.randomUUID());
        // For platform payments (Organisation), refund might need organisationId.
        // But this fixture seems to create member payment refund.
        return refund;
    }

    public static GymInvoice createInvoice(UUID organisationId, UUID subscriptionId) {
        return GymInvoice.builder()
                .organisationId(organisationId)
                .stripeInvoiceId("inv_test" + System.currentTimeMillis())
                .invoiceNumber("INV-" + System.currentTimeMillis())
                .amount(new BigDecimal("29.99"))
                .currency("usd")
                .status(InvoiceStatus.OPEN)
                .dueDate(LocalDateTime.now().plusDays(30))
                .description("Platform subscription invoice")
                .build();
    }

    // ==================== HELPER METHODS ====================

    public static String generateUniqueEmail() {
        return "test-" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
    }

    public static String generateUniquePhone() {
        return "+1" + (1000000000L + (long) (Math.random() * 9000000000L));
    }
}
