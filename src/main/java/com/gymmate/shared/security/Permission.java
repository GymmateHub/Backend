package com.gymmate.shared.security;

/**
 * What a signed-in user may do. Endpoints declare the permission they need, e.g.
 * {@code @PreAuthorize("hasAuthority('members:manage')")}; {@link RolePermissions} grants
 * permissions to roles in one place. The frontend receives the same list (login and
 * {@code /api/users/me}) and shows screens from it.
 */
public enum Permission {

    /** GymMateHub platform administration: tenants, tiers, platform-wide reports. */
    PLATFORM_ADMIN("platform:admin"),

    /** Read the organisation and its gyms; switch gym context. */
    ORG_VIEW("org:view"),
    /** Organisation and gym settings, gyms, branding, SMTP/WhatsApp configuration. */
    ORG_MANAGE("org:manage"),
    /** Platform subscription, payment methods, Stripe Connect, paying out refunds, deleting gyms. */
    ORG_BILLING("org:billing"),

    /** See the team (staff, trainers, managers). */
    STAFF_VIEW("staff:view"),
    /** Invite managers, staff and trainers. */
    STAFF_INVITE("staff:invite"),
    /** Change team members' roles, profiles and active state. */
    STAFF_MANAGE("staff:manage"),

    MEMBERS_VIEW("members:view"),
    /** Add and edit members, their memberships and their status. */
    MEMBERS_MANAGE("members:manage"),

    PLANS_VIEW("plans:view"),
    PLANS_MANAGE("plans:manage"),

    LEADS_MANAGE("leads:manage"),

    /** Ring up sales and run the cash drawer. */
    POS_OPERATE("pos:operate"),
    /** POS refunds, drawer history and sales reports. */
    POS_MANAGE("pos:manage"),

    /** Retail stock, suppliers, equipment and maintenance. */
    INVENTORY_MANAGE("inventory:manage"),

    /** Door access: scans, credentials, access events. */
    ACCESS_OPERATE("access:operate"),

    CLASSES_VIEW("classes:view"),
    /** Classes, categories, areas and schedules. */
    CLASSES_MANAGE("classes:manage"),
    /** Bookings on behalf of members, check-in and check-out. */
    BOOKINGS_MANAGE("bookings:manage"),

    /** Any member's health data: metrics, workouts, goals, AI plans. */
    HEALTH_MANAGE("health:manage"),

    ANALYTICS_VIEW("analytics:view"),
    /** Review and approve member refund requests. */
    REFUNDS_MANAGE("refunds:manage"),

    NOTIFICATIONS_VIEW("notifications:view"),
    /** Newsletters, campaigns and templates. */
    NOTIFICATIONS_MANAGE("notifications:manage"),

    /** A member acting on their own data: bookings, memberships, health, refunds, AI plan. */
    SELF_SERVICE("self:service");

    private final String authority;

    Permission(String authority) {
        this.authority = authority;
    }

    /** The granted-authority string used in {@code hasAuthority(...)}. */
    public String authority() {
        return authority;
    }
}
