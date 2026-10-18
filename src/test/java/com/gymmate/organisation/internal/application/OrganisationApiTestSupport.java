package com.gymmate.organisation.internal.application;

import com.gymmate.organisation.api.dto.GymSummary;
import com.gymmate.organisation.internal.domain.Gym;

/** Test helper: renders the Gym aggregate as the organisation facade's public read model. */
public final class OrganisationApiTestSupport {

    private OrganisationApiTestSupport() {
    }

    public static GymSummary summary(Gym gym) {
        return OrganisationApiService.toSummary(gym);
    }
}
