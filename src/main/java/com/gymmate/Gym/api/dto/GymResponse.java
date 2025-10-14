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
    private UUID gymId;
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
        return GymResponse.builder()
                .id(gym.getId())
                .gymId(gym.getGymId())
                .name(gym.getName())
                .description(gym.getDescription())
                .address(AddressResponse.fromValueObject(gym.getAddress()))
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

        public static AddressResponse fromValueObject(com.gymmate.Gym.domain.Address address) {
            if (address == null) {
                return null;
            }
            return AddressResponse.builder()
                    .street(address.getStreet())
                    .city(address.getCity())
                    .state(address.getState())
                    .postalCode(address.getPostalCode())
                    .country(address.getCountry())
                    .build();
        }
    }
}
