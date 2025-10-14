package com.gymmate.Gym.domain;

import com.gymmate.shared.domain.BaseEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Gym domain entity representing a gym facility.
 */
@Entity
@Table(name = "gyms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Gym extends BaseEntity {

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

    @Column(name = "owner_id", nullable = false)
    private UUID ownerId; // References User.id

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GymStatus status;

    public Gym(String name, String description, Address address, String contactEmail, String contactPhone, UUID ownerId) {
        validateInputs(name, address, contactEmail, contactPhone, ownerId);

        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.address = address;
        this.contactEmail = contactEmail.toLowerCase().trim();
        this.contactPhone = contactPhone.trim();
        this.ownerId = ownerId;
        this.status = GymStatus.ACTIVE;

        // A gym is its own tenant, so set gymId to its own ID
        setGymId(getId());
    }

    public void updateDetails(String name, String description, Address address, String contactEmail, String contactPhone) {
        validateUpdateInputs(name, address, contactEmail, contactPhone);

        this.name = name.trim();
        this.description = description != null ? description.trim() : null;
        this.address = address;
        this.contactEmail = contactEmail.toLowerCase().trim();
        this.contactPhone = contactPhone.trim();
    }

    public void activate() {
        if (this.status == GymStatus.ACTIVE) {
            throw new DomainException("GYM_ALREADY_ACTIVE", "Gym is already active");
        }
        this.status = GymStatus.ACTIVE;
        setActive(true);
    }

    public void deactivate() {
        if (this.status == GymStatus.INACTIVE) {
            throw new DomainException("GYM_ALREADY_INACTIVE", "Gym is already inactive");
        }
        this.status = GymStatus.INACTIVE;
        setActive(false);
    }

    public void suspend() {
        this.status = GymStatus.SUSPENDED;
        setActive(false);
    }

    public boolean isActive() {
        return status == GymStatus.ACTIVE;
    }

    private void validateInputs(String name, Address address, String contactEmail, String contactPhone, UUID ownerId) {
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

    @PrePersist
    @Override
    protected void prePersist() {
        super.prePersist();
        // Ensure a gym is its own tenant
        setGymId(getId());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Gym)) return false;
        return getId() != null && getId().equals(((Gym) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
