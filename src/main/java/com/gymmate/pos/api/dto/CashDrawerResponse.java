package com.gymmate.pos.api.dto;

import com.gymmate.pos.domain.CashDrawer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for CashDrawer entity.
 */
public record CashDrawerResponse(
        UUID id,
        UUID gymId,
        LocalDate sessionDate,
        UUID openedBy,
        UUID closedBy,
        BigDecimal openingBalance,
        BigDecimal closingBalance,
        BigDecimal expectedBalance,
        BigDecimal variance,
        BigDecimal totalCashSales,
        BigDecimal totalCardSales,
        BigDecimal totalOtherSales,
        BigDecimal totalRefunds,
        BigDecimal totalSales,
        Integer transactionCount,
        LocalDateTime openedAt,
        LocalDateTime closedAt,
        boolean open,
        String notes,
        String closingNotes,
        boolean hasVariance) {
    public static CashDrawerResponse fromEntity(CashDrawer drawer) {
        return new CashDrawerResponse(
                drawer.getId(),
                drawer.getGymId(),
                drawer.getSessionDate(),
                drawer.getOpenedBy(),
                drawer.getClosedBy(),
                drawer.getOpeningBalance(),
                drawer.getClosingBalance(),
                drawer.getExpectedBalance(),
                drawer.getVariance(),
                drawer.getTotalCashSales(),
                drawer.getTotalCardSales(),
                drawer.getTotalOtherSales(),
                drawer.getTotalRefunds(),
                drawer.getTotalSales(),
                drawer.getTransactionCount(),
                drawer.getOpenedAt(),
                drawer.getClosedAt(),
                drawer.isOpen(),
                drawer.getNotes(),
                drawer.getClosingNotes(),
                drawer.hasVariance());
    }
}
