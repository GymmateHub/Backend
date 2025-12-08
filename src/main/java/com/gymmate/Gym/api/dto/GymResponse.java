package com.gymmate.Gym.api.dto;

import com.gymmate.Gym.domain.Gym;
import com.gymmate.Gym.domain.GymStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for gym responses as a record.
 */
public record GymResponse(
    UUID id,
    String name,
    String description,
    AddressResponse address,
    String contactEmail,
    String contactPhone,
    UUID ownerId,
    GymStatus status,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    boolean active
) {

    public static GymResponse fromEntity(Gym gym) {
        AddressResponse addressResponse = null;
        if (gym.getAddress() != null || gym.getCity() != null) {
            addressResponse = new AddressResponse(
                    gym.getAddress(),
                    gym.getCity(),
                    gym.getState(),
                    gym.getPostalCode(),
                    gym.getCountry()
            );
        }

        return new GymResponse(
                gym.getId(),
                gym.getName(),
                gym.getDescription(),
                addressResponse,
                gym.getContactEmail(),
                gym.getContactPhone(),
                gym.getOwnerId(),
                gym.getStatus(),
                gym.getCreatedAt(),
                gym.getUpdatedAt(),
                gym.isActive()
        );
    }

    public record AddressResponse(
            String street,
            String city,
            String state,
            String postalCode,
            String country
    ) {}
}
