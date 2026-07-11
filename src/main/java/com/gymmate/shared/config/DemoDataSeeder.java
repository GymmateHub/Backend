package com.gymmate.shared.config;

import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.organisation.application.OrganisationService;
import com.gymmate.organisation.domain.Organisation;
import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.MemberRepository;
import com.gymmate.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Seeds a demo organisation, gym and one user per role for development.
 * Runs only when app.seed-demo-data=true (APP_SEED_DEMO_DATA in .env).
 * Idempotent: skipped entirely if the demo owner already exists.
 *
 * Credentials (all emails end in @demo.gymmatehub.com):
 *   owner / manager / trainer / staff / member
 *   password pattern: <Role>#@1357 (e.g. Owner#@1357)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DemoDataSeeder {

    private static final String DOMAIN = "@demo.gymmatehub.com";
    private static final String OWNER_EMAIL = "owner" + DOMAIN;

    private final UserRepository userRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final OrganisationService organisationService;
    private final GymService gymService;

    @Value("${app.seed-demo-data:false}")
    private boolean seedDemoData;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void seed() {
        if (!seedDemoData) {
            return;
        }
        if (userRepository.findByEmail(OWNER_EMAIL).isPresent()) {
            log.debug("Demo data already seeded, skipping");
            return;
        }

        try {
            TenantContext.clear();
            log.info("Seeding demo organisation, gym and role users...");

            // 1. Owner + organisation (createHub also creates the starter subscription)
            User owner = createUser("Olu", "Owner", OWNER_EMAIL, "Owner#@1357", UserRole.OWNER, null);
            Organisation organisation = organisationService.createHub("Demo Fitness Hub", OWNER_EMAIL, owner);
            UUID orgId = organisation.getId();

            // 2. Gym
            Gym gym = new Gym("Demo Gym Lagos", "Demo gym seeded for development",
                    OWNER_EMAIL, "08000000000", orgId);
            gym.setOrganisationId(orgId);
            gym = gymService.saveGym(gym);

            // 3. One user per remaining role
            createUser("Mary", "Manager", "manager" + DOMAIN, "Manager#@1357", UserRole.MANAGER, orgId);
            createUser("Tunde", "Trainer", "trainer" + DOMAIN, "Trainer#@1357", UserRole.TRAINER, orgId);
            createUser("Sade", "Staff", "staff" + DOMAIN, "Staff#@1357", UserRole.STAFF, orgId);
            User memberUser = createUser("Mike", "Member", "member" + DOMAIN, "Member#@1357",
                    UserRole.MEMBER, orgId);

            // 4. Member profile for the member user
            Member member = Member.builder()
                    .userId(memberUser.getId())
                    .membershipNumber("DEMO-0001")
                    .build();
            member.setOrganisationId(orgId);
            member.setGymId(gym.getId());
            memberRepository.save(member);

            log.info("Demo data seeded: organisation '{}' ({}), gym '{}', users owner/manager/trainer/staff/member{}",
                    organisation.getName(), organisation.getSlug(), gym.getName(), DOMAIN);
        } finally {
            TenantContext.clear();
        }
    }

    private User createUser(String firstName, String lastName, String email, String rawPassword,
            UserRole role, UUID organisationId) {
        User user = User.builder()
                .email(email)
                .firstName(firstName)
                .lastName(lastName)
                .passwordHash(passwordEncoder.encode(rawPassword))
                .phone("08000000001")
                .role(role)
                .status(UserStatus.ACTIVE)
                .emailVerified(true)
                .build();
        if (organisationId != null) {
            user.setOrganisationId(organisationId);
        }
        return userRepository.save(user);
    }
}
