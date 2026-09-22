package com.gymmate.shared.service;

import java.util.Locale;
import java.util.Map;

/**
 * Resolves the default billing currency (ISO 4217) for a gym from its country.
 *
 * <p>GymMateHub is Nigeria-first, so a gym registered without a country defaults to NGN.
 * A country that is supplied but not in the table below falls back to USD.
 */
public final class CurrencyResolver {

    public static final String PLATFORM_DEFAULT = "NGN";
    public static final String UNKNOWN_COUNTRY_FALLBACK = "USD";

    private static final Map<String, String> BY_COUNTRY = Map.ofEntries(
            Map.entry("NG", "NGN"), Map.entry("NIGERIA", "NGN"),
            Map.entry("GH", "GHS"), Map.entry("GHANA", "GHS"),
            Map.entry("KE", "KES"), Map.entry("KENYA", "KES"),
            Map.entry("ZA", "ZAR"), Map.entry("SOUTH AFRICA", "ZAR"),
            Map.entry("GB", "GBP"), Map.entry("UK", "GBP"), Map.entry("UNITED KINGDOM", "GBP"),
            Map.entry("US", "USD"), Map.entry("USA", "USD"), Map.entry("UNITED STATES", "USD"),
            Map.entry("CA", "CAD"), Map.entry("CANADA", "CAD"),
            Map.entry("AU", "AUD"), Map.entry("AUSTRALIA", "AUD"),
            Map.entry("DE", "EUR"), Map.entry("GERMANY", "EUR"),
            Map.entry("FR", "EUR"), Map.entry("FRANCE", "EUR"),
            Map.entry("ES", "EUR"), Map.entry("SPAIN", "EUR"),
            Map.entry("IT", "EUR"), Map.entry("ITALY", "EUR"),
            Map.entry("NL", "EUR"), Map.entry("NETHERLANDS", "EUR"),
            Map.entry("IE", "EUR"), Map.entry("IRELAND", "EUR"));

    private CurrencyResolver() {
    }

    /**
     * @param country ISO 3166 alpha-2 code or English country name; may be null/blank
     * @return ISO 4217 currency code, never null
     */
    public static String forCountry(String country) {
        if (country == null || country.isBlank()) {
            return PLATFORM_DEFAULT;
        }
        return BY_COUNTRY.getOrDefault(country.trim().toUpperCase(Locale.ROOT), UNKNOWN_COUNTRY_FALLBACK);
    }
}
