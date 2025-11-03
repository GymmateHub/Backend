package com.gymmate.shared.config;

import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class SuperAdminInitializer {
    private static final String DEFAULT_PHONE_NUMBER = "00000000000";
    private static final UUID DEFAULT_TENANT_ID = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AdminConfig adminConfig;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void initializeSuperAdmin() {
        try {
            TenantContext.setCurrentTenantId(DEFAULT_TENANT_ID);
            validateAdminConfig();
            String adminEmail = adminConfig.getEmail();

            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                log.info("Initializing default super admin user with email: {}", adminEmail);

                User superAdmin = User.builder()
                        .email(adminEmail)
                        .passwordHash(passwordEncoder.encode(adminConfig.getPassword()))
                        .firstName(adminConfig.getFirstName())
                        .lastName(adminConfig.getLastName())
                        .phoneNumber(DEFAULT_PHONE_NUMBER)
                        .role(UserRole.SUPER_ADMIN)
                        .status(UserStatus.ACTIVE)
                        .emailVerified(true)
                        .build();

                // Set tenant ID for super admin
                superAdmin.setGymId(DEFAULT_TENANT_ID);

                userRepository.save(superAdmin);
                log.info("Successfully created super admin user");
            } else {
                log.debug("Super admin user already exists with email: {}, skipping initialization", adminEmail);
            }
        } finally {
            TenantContext.clear();
        }
    }

    private void validateAdminConfig() {
        if (!StringUtils.hasText(adminConfig.getEmail())) {
            throw new IllegalStateException("Admin email must be configured in application properties");
        }
        if (!StringUtils.hasText(adminConfig.getPassword())) {
            throw new IllegalStateException("Admin password must be configured in application properties");
        }
        if (!StringUtils.hasText(adminConfig.getFirstName())) {
            throw new IllegalStateException("Admin first name must be configured in application properties");
        }
        if (!StringUtils.hasText(adminConfig.getLastName())) {
            throw new IllegalStateException("Admin last name must be configured in application properties");
        }
    }
}
