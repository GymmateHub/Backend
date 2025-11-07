package com.gymmate.Gym.api.dto;

import com.gymmate.Gym.domain.Gym;
import com.gymmate.Gym.domain.GymStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for gym responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymResponse {

    private UUID id;
    private String name;
    private String description;
    private AddressResponse address;
    private String contactEmail;
    private String contactPhone;
    private UUID ownerId;
    private GymStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private boolean active;

    /**
     * Create a response DTO from a domain entity.
     */
    public static GymResponse fromEntity(Gym gym) {
        AddressResponse addressResponse = null;
        if (gym.getAddress() != null || gym.getCity() != null) {
            addressResponse = AddressResponse.builder()
                    .street(gym.getAddress())
                    .city(gym.getCity())
                    .state(gym.getState())
                    .postalCode(gym.getPostalCode())
                    .country(gym.getCountry())
                    .build();
        }

        return GymResponse.builder()
                .id(gym.getId())
                .name(gym.getName())
                .description(gym.getDescription())
                .address(addressResponse)
                .contactEmail(gym.getContactEmail())
                .contactPhone(gym.getContactPhone())
                .ownerId(gym.getOwnerId())
                .status(gym.getStatus())
                .createdAt(gym.getCreatedAt())
                .updatedAt(gym.getUpdatedAt())
                .active(gym.isActive())
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddressResponse {
        private String street;
        private String city;
        private String state;
        private String postalCode;
        private String country;
    }
}
