package com.gymmate.identity.internal.application;

import com.gymmate.identity.api.dto.MemberProfile;
import com.gymmate.identity.api.dto.UserSummary;
import com.gymmate.identity.internal.domain.Member;
import com.gymmate.identity.internal.domain.User;

/**
 * Test helper: renders identity aggregates as the public read models the identity facade
 * returns, so tests of other modules can keep building fixtures with the aggregates.
 */
public final class IdentityApiTestSupport {

    private IdentityApiTestSupport() {
    }

    public static MemberProfile profile(Member member) {
        return IdentityApiService.toProfile(member);
    }

    public static UserSummary summary(User user) {
        return IdentityApiService.toSummary(user);
    }
}
