/**
 * TRANSITIONAL grant (clean-architecture refactor, Step C removes it): MemberMembershipRepository is read directly by access and scheduling until membership.api.MembershipApi exposes membership status checks.
 */
@org.springframework.modulith.NamedInterface("application.port")
package com.gymmate.membership.internal.application.port;
