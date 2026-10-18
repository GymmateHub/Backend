package com.gymmate.shared.security;

import com.gymmate.shared.constants.UserRole;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gymmate.shared.security.Permission.*;

/**
 * The single role-to-permission table.
 *
 * <ul>
 *   <li>GYM_OWNER: the organisation's administrator; everything in the organisation.</li>
 *   <li>MANAGER: runs the gyms day to day; no billing, no team management.</li>
 *   <li>STAFF: front desk; members, leads, POS, inventory, door access, bookings.</li>
 *   <li>TRAINER: classes, bookings and members' health; reads members.</li>
 *   <li>MEMBER: their own data only.</li>
 *   <li>SUPER_ADMIN: the platform, plus every organisation permission when acting inside an
 *       organisation (see {@code TenantFilter}).</li>
 * </ul>
 * Staff and trainers work across all gyms of their organisation.
 */
public final class RolePermissions {

    private static final Set<Permission> OWNER = EnumSet.complementOf(EnumSet.of(PLATFORM_ADMIN, SELF_SERVICE));

    private static final Set<Permission> MANAGER = EnumSet.of(
            ORG_VIEW, ORG_MANAGE, STAFF_VIEW,
            MEMBERS_VIEW, MEMBERS_MANAGE, PLANS_VIEW, PLANS_MANAGE,
            LEADS_MANAGE, POS_OPERATE, POS_MANAGE, INVENTORY_MANAGE, ACCESS_OPERATE,
            CLASSES_VIEW, CLASSES_MANAGE, BOOKINGS_MANAGE, HEALTH_MANAGE,
            ANALYTICS_VIEW, REFUNDS_MANAGE, NOTIFICATIONS_VIEW, NOTIFICATIONS_MANAGE);

    private static final Set<Permission> STAFF = EnumSet.of(
            ORG_VIEW, MEMBERS_VIEW, MEMBERS_MANAGE, PLANS_VIEW,
            LEADS_MANAGE, POS_OPERATE, INVENTORY_MANAGE, ACCESS_OPERATE,
            CLASSES_VIEW, BOOKINGS_MANAGE, NOTIFICATIONS_VIEW);

    private static final Set<Permission> TRAINER = EnumSet.of(
            ORG_VIEW, MEMBERS_VIEW, PLANS_VIEW,
            CLASSES_VIEW, CLASSES_MANAGE, BOOKINGS_MANAGE, HEALTH_MANAGE, NOTIFICATIONS_VIEW);

    private static final Set<Permission> MEMBER = EnumSet.of(PLANS_VIEW, CLASSES_VIEW, SELF_SERVICE);

    private static final Map<UserRole, Set<Permission>> BY_ROLE = new EnumMap<>(UserRole.class);

    static {
        BY_ROLE.put(UserRole.SUPER_ADMIN, EnumSet.complementOf(EnumSet.of(SELF_SERVICE)));
        BY_ROLE.put(UserRole.GYM_OWNER, OWNER);
        // Legacy organisation administrators; no longer invitable, treated as owners.
        BY_ROLE.put(UserRole.ADMIN, OWNER);
        BY_ROLE.put(UserRole.MANAGER, MANAGER);
        BY_ROLE.put(UserRole.STAFF, STAFF);
        BY_ROLE.put(UserRole.TRAINER, TRAINER);
        BY_ROLE.put(UserRole.MEMBER, MEMBER);
    }

    /** Roles a Gym Owner may invite into their organisation. */
    public static final Set<UserRole> INVITABLE_ROLES = Collections.unmodifiableSet(
            EnumSet.of(UserRole.MANAGER, UserRole.STAFF, UserRole.TRAINER));

    private RolePermissions() {
    }

    public static Set<Permission> of(UserRole role) {
        return role == null ? Set.of() : Collections.unmodifiableSet(BY_ROLE.getOrDefault(role, Set.of()));
    }

    public static Set<Permission> of(String roleName) {
        try {
            return of(UserRole.valueOf(roleName));
        } catch (IllegalArgumentException | NullPointerException e) {
            return Set.of();
        }
    }

    /** Authority strings for a role, sorted; what the frontend receives. */
    public static List<String> authorities(String roleName) {
        return of(roleName).stream().map(Permission::authority).sorted().toList();
    }
}
