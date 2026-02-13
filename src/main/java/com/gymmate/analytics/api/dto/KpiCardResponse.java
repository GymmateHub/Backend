package com.gymmate.analytics.api.dto;

import java.math.BigDecimal;

/**
 * Dashboard KPI card response DTO.
 */
public record KpiCardResponse(
        String title,
        String value,
        String previousValue,
        BigDecimal changePercentage,
        boolean isPositiveChange,
        String icon,
        String color,
        String description) {
    public static KpiCardResponse of(String title, long currentValue, long previousValue, String icon, String color) {
        BigDecimal changePercentage = calculateChangePercentage(
                BigDecimal.valueOf(currentValue),
                BigDecimal.valueOf(previousValue));
        return new KpiCardResponse(
                title,
                String.valueOf(currentValue),
                String.valueOf(previousValue),
                changePercentage,
                changePercentage.compareTo(BigDecimal.ZERO) >= 0,
                icon,
                color,
                null);
    }

    public static KpiCardResponse ofMoney(String title, BigDecimal currentValue, BigDecimal previousValue, String icon,
            String color) {
        BigDecimal changePercentage = calculateChangePercentage(currentValue, previousValue);
        return new KpiCardResponse(
                title,
                formatMoney(currentValue),
                formatMoney(previousValue),
                changePercentage,
                changePercentage.compareTo(BigDecimal.ZERO) >= 0,
                icon,
                color,
                null);
    }

    public static KpiCardResponse ofPercentage(String title, BigDecimal currentValue, BigDecimal previousValue,
            String icon, String color) {
        BigDecimal changePercentage = currentValue.subtract(previousValue);
        return new KpiCardResponse(
                title,
                currentValue.setScale(1, java.math.RoundingMode.HALF_UP) + "%",
                previousValue.setScale(1, java.math.RoundingMode.HALF_UP) + "%",
                changePercentage,
                changePercentage.compareTo(BigDecimal.ZERO) >= 0,
                icon,
                color,
                null);
    }

    private static BigDecimal calculateChangePercentage(BigDecimal current, BigDecimal previous) {
        if (previous == null || previous.compareTo(BigDecimal.ZERO) == 0) {
            return current.compareTo(BigDecimal.ZERO) > 0 ? BigDecimal.valueOf(100) : BigDecimal.ZERO;
        }
        return current.subtract(previous)
                .divide(previous, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(1, java.math.RoundingMode.HALF_UP);
    }

    private static String formatMoney(BigDecimal value) {
        if (value == null)
            return "$0.00";
        return "$" + value.setScale(2, java.math.RoundingMode.HALF_UP).toPlainString();
    }
}
