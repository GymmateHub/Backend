package com.gymmate.unit.gym.api;

import com.gymmate.gym.api.dto.GymResponse;
import com.gymmate.gym.domain.Gym;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GymResponseTest {

    private Gym newGym() {
        return new Gym("Iron Temple", "Main Gym", "owner@example.com", "+2348000000000", UUID.randomUUID());
    }

    @Test
    void exposesSettingsTheFrontendReads() {
        Gym gym = newGym();
        gym.setCurrency("NGN");
        gym.setTimezone("Africa/Lagos");
        gym.setLogoUrl("https://cdn.example.com/logo.png");
        gym.setWebsite("https://irontemple.example.com");
        gym.setBusinessHours("{\"monday\":{\"open\":\"06:00\",\"close\":\"22:00\",\"active\":true}}");

        GymResponse response = GymResponse.fromEntity(gym);

        assertEquals("NGN", response.currency());
        assertEquals("Africa/Lagos", response.timezone());
        assertEquals("https://cdn.example.com/logo.png", response.logoUrl());
        assertEquals("https://irontemple.example.com", response.website());
        assertEquals(gym.getBusinessHours(), response.businessHours());
        assertEquals("+2348000000000", response.contactPhone());
    }

    @Test
    void optionalSettingsAreNullWhenNeverConfigured() {
        GymResponse response = GymResponse.fromEntity(newGym());

        assertNull(response.logoUrl());
        assertNull(response.website());
        assertNull(response.businessHours());
    }
}
