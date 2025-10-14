package com.gymmate.Gym.domain;

import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.StringUtils;

import java.util.Objects;

/**
 * Address value object for representing gym locations.
 */
@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Address {

    @Column(nullable = false)
    private String street;

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private String country;

    public Address(String street, String city, String state, String postalCode, String country) {
        validateInputs(street, city, state, postalCode, country);

        this.street = street.trim();
        this.city = city.trim();
        this.state = state.trim();
        this.postalCode = postalCode.trim();
        this.country = country.trim();
    }

    public String getFullAddress() {
        return String.format("%s, %s, %s %s, %s", street, city, state, postalCode, country);
    }

    private void validateInputs(String street, String city, String state, String postalCode, String country) {
        if (!StringUtils.hasText(street)) {
            throw new DomainException("INVALID_STREET", "Street cannot be empty");
        }
        if (!StringUtils.hasText(city)) {
            throw new DomainException("INVALID_CITY", "City cannot be empty");
        }
        if (!StringUtils.hasText(state)) {
            throw new DomainException("INVALID_STATE", "State cannot be empty");
        }
        if (!StringUtils.hasText(postalCode)) {
            throw new DomainException("INVALID_POSTAL_CODE", "Postal code cannot be empty");
        }
        if (!StringUtils.hasText(country)) {
            throw new DomainException("INVALID_COUNTRY", "Country cannot be empty");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Address address = (Address) o;
        return Objects.equals(street, address.street) &&
               Objects.equals(city, address.city) &&
               Objects.equals(state, address.state) &&
               Objects.equals(postalCode, address.postalCode) &&
               Objects.equals(country, address.country);
    }

    @Override
    public int hashCode() {
        return Objects.hash(street, city, state, postalCode, country);
    }

    @Override
    public String toString() {
        return getFullAddress();
    }
}
