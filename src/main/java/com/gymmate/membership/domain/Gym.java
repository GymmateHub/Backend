package com.gymmate.membership.domain;

import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Gym domain entity representing a gym facility.
 */
@Entity
@Table(name = "gyms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Embedded
    private Address address;

    @Column(nullable = false)
    private String contactEmail;

    @Column(nullable = false)
    private String contactPhone;

    @Column(nullable = false)
    private Long ownerId; // References User.id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GymStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public Gym(String name, String description, Address address, String contactEmail, String contactPhone, Long ownerId) {
        validateInputs(name, address, contactEmail, contactPhone, ownerId);
        
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.address = address;
        this.contactEmail = contactEmail.toLowerCase().trim();
        this.contactPhone = contactPhone.trim();
        this.ownerId = ownerId;
        this.status = GymStatus.ACTIVE;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateDetails(String name, String description, Address address, String contactEmail, String contactPhone) {
        validateUpdateInputs(name, address, contactEmail, contactPhone);
        
        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.address = address;
        this.contactEmail = contactEmail.toLowerCase().trim();
        this.contactPhone = contactPhone.trim();
        this.updatedAt = LocalDateTime.now();
    }

    public void activate() {
        if (this.status == GymStatus.ACTIVE) {
            throw new DomainException("GYM_ALREADY_ACTIVE", "Gym is already active");
        }
        this.status = GymStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        if (this.status == GymStatus.INACTIVE) {
            throw new DomainException("GYM_ALREADY_INACTIVE", "Gym is already inactive");
        }
        this.status = GymStatus.INACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    public void suspend() {
        this.status = GymStatus.SUSPENDED;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return status == GymStatus.ACTIVE;
    }

    private void validateInputs(String name, Address address, String contactEmail, String contactPhone, Long ownerId) {
        if (!StringUtils.hasText(name)) {
            throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
        }
        if (address == null) {
            throw new DomainException("INVALID_ADDRESS", "Address cannot be null");
        }
        if (!StringUtils.hasText(contactEmail)) {
            throw new DomainException("INVALID_CONTACT_EMAIL", "Contact email cannot be empty");
        }
        if (!StringUtils.hasText(contactPhone)) {
            throw new DomainException("INVALID_CONTACT_PHONE", "Contact phone cannot be empty");
        }
        if (ownerId == null) {
            throw new DomainException("INVALID_OWNER", "Owner ID cannot be null");
        }
    }

    private void validateUpdateInputs(String name, Address address, String contactEmail, String contactPhone) {
        if (!StringUtils.hasText(name)) {
            throw new DomainException("INVALID_GYM_NAME", "Gym name cannot be empty");
        }
        if (address == null) {
            throw new DomainException("INVALID_ADDRESS", "Address cannot be null");
        }
        if (!StringUtils.hasText(contactEmail)) {
            throw new DomainException("INVALID_CONTACT_EMAIL", "Contact email cannot be empty");
        }
        if (!StringUtils.hasText(contactPhone)) {
            throw new DomainException("INVALID_CONTACT_PHONE", "Contact phone cannot be empty");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Gym gym = (Gym) o;
        return Objects.equals(id, gym.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}