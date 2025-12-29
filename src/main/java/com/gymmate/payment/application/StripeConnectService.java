package com.gymmate.payment.application;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.payment.api.dto.ConnectAccountStatusResponse;
import com.gymmate.payment.api.dto.ConnectOnboardingResponse;
import com.gymmate.shared.config.StripeConfig;
import com.gymmate.shared.exception.DomainException;
import com.stripe.exception.StripeException;
import com.stripe.model.Account;
import com.stripe.model.AccountLink;
import com.stripe.model.LoginLink;
import com.stripe.param.AccountCreateParams;
import com.stripe.param.AccountLinkCreateParams;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

/**
 * Service for handling Stripe Connect operations.
 * Enables gyms to receive payments from their members.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class StripeConnectService {

    private final StripeConfig stripeConfig;
    private final GymRepository gymRepository;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    /**
     * Start the Stripe Connect onboarding process for a gym.
     * Creates a Connect Express account and returns the onboarding URL.
     */
    @Transactional
    public ConnectOnboardingResponse startOnboarding(UUID gymId) {
        Gym gym = getGym(gymId);
        validateStripeConfigured();

        try {
            String accountId;

            // Check if gym already has a Connect account
            if (gym.getStripeConnectAccountId() != null) {
                accountId = gym.getStripeConnectAccountId();
                log.info("Gym {} already has Connect account {}, creating refresh link", gymId, accountId);
            } else {
                // Create a new Stripe Connect Express account
                AccountCreateParams params = AccountCreateParams.builder()
                        .setType(AccountCreateParams.Type.EXPRESS)
                        .setEmail(gym.getContactEmail())
                        .setBusinessType(AccountCreateParams.BusinessType.COMPANY)
                        .setCompany(AccountCreateParams.Company.builder()
                                .setName(gym.getName())
                                .build())
                        .putMetadata("gym_id", gymId.toString())
                        .setCapabilities(AccountCreateParams.Capabilities.builder()
                                .setCardPayments(AccountCreateParams.Capabilities.CardPayments.builder()
                                        .setRequested(true)
                                        .build())
                                .setTransfers(AccountCreateParams.Capabilities.Transfers.builder()
                                        .setRequested(true)
                                        .build())
                                .build())
                        .build();

                Account account = Account.create(params);
                accountId = account.getId();

                // Save the account ID to the gym
                gym.setStripeConnectAccountId(accountId);
                gymRepository.save(gym);

                log.info("Created Stripe Connect account {} for gym {}", accountId, gymId);
            }

            // Create the account onboarding link
            String onboardingUrl = createOnboardingLink(accountId, gymId);

            return ConnectOnboardingResponse.builder()
                    .accountId(accountId)
                    .onboardingUrl(onboardingUrl)
                    .build();

        } catch (StripeException e) {
            log.error("Failed to start Connect onboarding for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_CONNECT_ONBOARDING_FAILED",
                    "Failed to start payment setup: " + e.getMessage());
        }
    }

    /**
     * Create an account link for onboarding or updating account details.
     */
    public String createOnboardingLink(String accountId, UUID gymId) throws StripeException {
        AccountLinkCreateParams params = AccountLinkCreateParams.builder()
                .setAccount(accountId)
                .setRefreshUrl(frontendUrl + "/gym/settings/payments?refresh=true&gymId=" + gymId)
                .setReturnUrl(frontendUrl + "/gym/settings/payments?success=true&gymId=" + gymId)
                .setType(AccountLinkCreateParams.Type.ACCOUNT_ONBOARDING)
                .build();

        AccountLink accountLink = AccountLink.create(params);
        return accountLink.getUrl();
    }

    /**
     * Get the Stripe Connect account status for a gym.
     */
    public ConnectAccountStatusResponse getAccountStatus(UUID gymId) {
        Gym gym = getGym(gymId);

        if (gym.getStripeConnectAccountId() == null) {
            return ConnectAccountStatusResponse.builder()
                    .chargesEnabled(false)
                    .payoutsEnabled(false)
                    .detailsSubmitted(false)
                    .requiresAction(true)
                    .build();
        }

        validateStripeConfigured();

        try {
            Account account = Account.retrieve(gym.getStripeConnectAccountId());

            // Update local cache of account status
            updateGymConnectStatus(gym, account);

            LocalDateTime deadline = null;
            if (account.getRequirements() != null &&
                account.getRequirements().getCurrentDeadline() != null) {
                deadline = LocalDateTime.ofInstant(
                        Instant.ofEpochSecond(account.getRequirements().getCurrentDeadline()),
                        ZoneId.systemDefault());
            }

            boolean requiresAction = account.getRequirements() != null &&
                    account.getRequirements().getCurrentlyDue() != null &&
                    !account.getRequirements().getCurrentlyDue().isEmpty();

            return ConnectAccountStatusResponse.builder()
                    .accountId(account.getId())
                    .chargesEnabled(account.getChargesEnabled())
                    .payoutsEnabled(account.getPayoutsEnabled())
                    .detailsSubmitted(account.getDetailsSubmitted())
                    .requiresAction(requiresAction)
                    .currentDeadline(deadline)
                    .build();

        } catch (StripeException e) {
            log.error("Failed to get Connect account status for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_CONNECT_STATUS_FAILED",
                    "Failed to get payment status: " + e.getMessage());
        }
    }

    /**
     * Get a link to the Stripe Express dashboard for the gym.
     */
    public String getDashboardLink(UUID gymId) {
        Gym gym = getGym(gymId);

        if (gym.getStripeConnectAccountId() == null) {
            throw new DomainException("STRIPE_CONNECT_NOT_SETUP",
                    "Payment account not set up. Please complete onboarding first.");
        }

        validateStripeConfigured();

        try {
            LoginLink loginLink = LoginLink.createOnAccount(gym.getStripeConnectAccountId());
            return loginLink.getUrl();

        } catch (StripeException e) {
            log.error("Failed to create dashboard link for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_DASHBOARD_LINK_FAILED",
                    "Failed to access payment dashboard: " + e.getMessage());
        }
    }

    /**
     * Create a refresh URL for incomplete onboarding.
     */
    @Transactional
    public ConnectOnboardingResponse refreshOnboardingLink(UUID gymId) {
        Gym gym = getGym(gymId);

        if (gym.getStripeConnectAccountId() == null) {
            // Start fresh onboarding if no account exists
            return startOnboarding(gymId);
        }

        validateStripeConfigured();

        try {
            String onboardingUrl = createOnboardingLink(gym.getStripeConnectAccountId(), gymId);

            return ConnectOnboardingResponse.builder()
                    .accountId(gym.getStripeConnectAccountId())
                    .onboardingUrl(onboardingUrl)
                    .build();

        } catch (StripeException e) {
            log.error("Failed to refresh onboarding link for gym {}: {}", gymId, e.getMessage());
            throw new DomainException("STRIPE_CONNECT_REFRESH_FAILED",
                    "Failed to refresh payment setup link: " + e.getMessage());
        }
    }

    /**
     * Check if a gym can accept payments.
     */
    public boolean canAcceptPayments(UUID gymId) {
        Gym gym = getGym(gymId);

        if (gym.getStripeConnectAccountId() == null) {
            return false;
        }

        // Check cached status first
        if (Boolean.TRUE.equals(gym.getStripeChargesEnabled())) {
            return true;
        }

        // Fetch fresh status from Stripe
        try {
            validateStripeConfigured();
            Account account = Account.retrieve(gym.getStripeConnectAccountId());
            updateGymConnectStatus(gym, account);
            return account.getChargesEnabled();
        } catch (StripeException e) {
            log.warn("Failed to check Connect account status for gym {}: {}", gymId, e.getMessage());
            return false;
        }
    }

    /**
     * Handle account.updated webhook event.
     */
    @Transactional
    public void handleAccountUpdated(String accountId) {
        try {
            Account account = Account.retrieve(accountId);
            String gymIdStr = account.getMetadata().get("gym_id");

            if (gymIdStr != null) {
                UUID gymId = UUID.fromString(gymIdStr);
                Gym gym = getGym(gymId);
                updateGymConnectStatus(gym, account);

                if (account.getChargesEnabled() && account.getPayoutsEnabled()) {
                    log.info("Gym {} Connect account {} is now fully active", gymId, accountId);
                }
            }
        } catch (StripeException e) {
            log.error("Failed to handle account.updated for {}: {}", accountId, e.getMessage());
        }
    }

    // Helper methods

    private Gym getGym(UUID gymId) {
        return gymRepository.findById(gymId)
                .orElseThrow(() -> new DomainException("GYM_NOT_FOUND", "Gym not found"));
    }

    private void validateStripeConfigured() {
        if (!stripeConfig.isConfigured()) {
            throw new DomainException("STRIPE_NOT_CONFIGURED",
                    "Payment processing is not configured. Please contact support.");
        }
    }

    @Transactional
    protected void updateGymConnectStatus(Gym gym, Account account) {
        gym.setStripeChargesEnabled(account.getChargesEnabled());
        gym.setStripePayoutsEnabled(account.getPayoutsEnabled());
        gym.setStripeDetailsSubmitted(account.getDetailsSubmitted());

        if (account.getChargesEnabled() && gym.getStripeOnboardingCompletedAt() == null) {
            gym.setStripeOnboardingCompletedAt(LocalDateTime.now());
        }

        gymRepository.save(gym);
    }
}

