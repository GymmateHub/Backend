package com.gymmate.membership.api.dto;

import com.gymmate.membership.domain.Gym;
import com.gymmate.membership.domain.GymStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for gym responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymResponse {
    
    private Long id;
    private String name;
    private String description;
    private AddressResponse address;
    private String contactEmail;
    private String contactPhone;
    private Long ownerId;
    private GymStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    public static GymResponse fromEntity(Gym gym) {
        return GymResponse.builder()
                .id(gym.getId())
                .name(gym.getName())
                .description(gym.getDescription())
                .address(AddressResponse.fromValueObject(gym.getAddress()))
                .contactEmail(gym.getContactEmail())
                .contactPhone(gym.getContactPhone())
                .ownerId(gym.getOwnerId())
                .status(gym.getStatus())
                .createdAt(gym.getCreatedAt())
                .updatedAt(gym.getUpdatedAt())
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
        private String fullAddress;
        
        public static AddressResponse fromValueObject(com.gymmate.membership.domain.Address address) {
            return AddressResponse.builder()
                    .street(address.getStreet())
                    .city(address.getCity())
                    .state(address.getState())
                    .postalCode(address.getPostalCode())
                    .country(address.getCountry())
                    .fullAddress(address.getFullAddress())
                    .build();
        }
    }
}