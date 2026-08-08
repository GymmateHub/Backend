/**
 * Extension points that let {@code AudienceResolver} (in the parent {@code application}
 * package) resolve newsletter audiences without depending on {@code classes},
 * {@code membership}, or {@code user} module internals directly. Introduced to break a
 * real module dependency cycle: {@code notification} previously queried those modules'
 * JPA repositories directly (in {@code AudienceResolver}), while they also depended on
 * {@code notification} for events/notifications — a cycle
 * {@code ApplicationModules.verify()} correctly rejects. Adapters implementing these
 * ports live in the producing modules ({@code classes.infrastructure},
 * {@code membership.infrastructure}, {@code user.infrastructure}), keeping the
 * dependency direction the same as everywhere else (producer depends on
 * {@code notification}, not the other way around) — same pattern as
 * {@code access.application.port.AccessDevicePort}.
 */
@org.springframework.modulith.NamedInterface("application.port")
package com.gymmate.notification.application.port;
