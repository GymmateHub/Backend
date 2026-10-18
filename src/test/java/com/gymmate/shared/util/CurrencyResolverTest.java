package com.gymmate.shared.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CurrencyResolverTest {

    @ParameterizedTest
    @CsvSource({
            "NG,NGN", "Nigeria,NGN", "  nigeria  ,NGN",
            "GH,GHS", "Kenya,KES", "South Africa,ZAR",
            "GB,GBP", "United Kingdom,GBP",
            "US,USD", "United States,USD",
            "Germany,EUR", "FR,EUR"
    })
    void resolvesKnownCountriesCaseInsensitively(String country, String expected) {
        assertEquals(expected, CurrencyResolver.forCountry(country));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void blankCountryDefaultsToPlatformCurrency(String country) {
        assertEquals(CurrencyResolver.PLATFORM_DEFAULT, CurrencyResolver.forCountry(country));
    }

    @Test
    void unknownCountryFallsBackToUsd() {
        assertEquals(CurrencyResolver.UNKNOWN_COUNTRY_FALLBACK, CurrencyResolver.forCountry("Atlantis"));
    }
}
