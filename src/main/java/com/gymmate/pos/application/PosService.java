package com.gymmate.pos.application;

import com.gymmate.inventory.application.InventoryService;
import com.gymmate.inventory.domain.InventoryItem;
import com.gymmate.pos.api.dto.CreateSaleRequest;
import com.gymmate.pos.api.dto.SaleItemRequest;
import com.gymmate.pos.domain.*;
import com.gymmate.pos.infrastructure.CashDrawerJpaRepository;
import com.gymmate.pos.infrastructure.SaleItemJpaRepository;
import com.gymmate.pos.infrastructure.SaleJpaRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Application service for POS (Point of Sale) operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PosService {

    private final SaleJpaRepository saleRepository;
    private final SaleItemJpaRepository saleItemRepository;
    private final CashDrawerJpaRepository cashDrawerRepository;
    private final InventoryService inventoryService;

    private static final AtomicLong saleCounter = new AtomicLong(System.currentTimeMillis());

    // ===== SALE OPERATIONS =====

    /**
     * Create a new sale transaction.
     */
    @Transactional
    public Sale createSale(CreateSaleRequest request, UUID staffId) {
        log.info("Creating new sale for gym: {}", request.gymId());

        Sale sale = Sale.builder()
                .saleNumber(generateSaleNumber())
                .memberId(request.memberId())
                .customerName(request.customerName())
                .staffId(staffId)
                .paymentType(request.paymentType())
                .discountPercentage(request.discountPercentage())
                .discountCode(request.discountCode())
                .taxRate(request.taxRate())
                .notes(request.notes())
                .status(SaleStatus.PENDING)
                .saleDate(LocalDateTime.now())
                .build();
        sale.setGymId(request.gymId());

        // Add sale items
        for (SaleItemRequest itemRequest : request.items()) {
            SaleItem item = createSaleItem(itemRequest, sale);
            sale.addItem(item);
        }

        sale.recalculateTotals();

        Sale savedSale = saleRepository.save(sale);
        log.info("Created sale: {} with {} items, total: {}",
                savedSale.getSaleNumber(), savedSale.getItems().size(), savedSale.getTotalAmount());

        return savedSale;
    }

    /**
     * Create sale and complete payment in one transaction.
     */
    @Transactional
    public Sale createAndCompleteSale(CreateSaleRequest request, UUID staffId, BigDecimal amountPaid) {
        Sale sale = createSale(request, staffId);
        return completeSale(sale.getId(), request.paymentType(), amountPaid, null);
    }

    /**
     * Complete a pending sale with payment.
     */
    @Transactional
    public Sale completeSale(UUID saleId, PaymentType paymentType, BigDecimal amountPaid,
            String stripePaymentIntentId) {
        Sale sale = getSaleById(saleId);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new IllegalStateException("Sale is not in pending status");
        }

        sale.setPaymentType(paymentType);
        if (stripePaymentIntentId != null) {
            sale.setStripePaymentIntentId(stripePaymentIntentId);
        }

        sale.complete(paymentType, amountPaid);

        // Update inventory for each sold item
        for (SaleItem item : sale.getItems()) {
            if (item.getInventoryItemId() != null) {
                try {
                    inventoryService.recordSale(
                            item.getInventoryItemId(),
                            item.getQuantity(),
                            item.getUnitPrice(),
                            sale.getMemberId(),
                            sale.getSaleNumber(),
                            "POS Sale: " + sale.getSaleNumber());
                } catch (Exception e) {
                    log.warn("Failed to update inventory for item {}: {}", item.getInventoryItemId(), e.getMessage());
                }
            }
        }

        // Update cash drawer if it's a cash sale
        if (paymentType == PaymentType.CASH) {
            updateCashDrawerForSale(sale.getGymId(), sale.getTotalAmount(), false);
        }

        Sale savedSale = saleRepository.save(sale);
        log.info("Completed sale: {} with payment {} amount {}",
                savedSale.getSaleNumber(), paymentType, amountPaid);

        return savedSale;
    }

    /**
     * Cancel a pending sale.
     */
    @Transactional
    public Sale cancelSale(UUID saleId) {
        Sale sale = getSaleById(saleId);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new IllegalStateException("Only pending sales can be cancelled");
        }

        sale.cancel();
        Sale savedSale = saleRepository.save(sale);
        log.info("Cancelled sale: {}", savedSale.getSaleNumber());

        return savedSale;
    }

    /**
     * Refund a completed sale (full or partial).
     */
    @Transactional
    public Sale refundSale(UUID saleId, BigDecimal refundAmount, String reason) {
        Sale sale = getSaleById(saleId);

        if (sale.getStatus() != SaleStatus.COMPLETED && sale.getStatus() != SaleStatus.PARTIALLY_REFUNDED) {
            throw new IllegalStateException("Sale cannot be refunded");
        }

        BigDecimal maxRefundable = sale.getTotalAmount()
                .subtract(sale.getRefundedAmount() != null ? sale.getRefundedAmount() : BigDecimal.ZERO);

        if (refundAmount.compareTo(maxRefundable) > 0) {
            throw new IllegalArgumentException("Refund amount exceeds available balance");
        }

        sale.refund(refundAmount);
        if (reason != null) {
            sale.setNotes((sale.getNotes() != null ? sale.getNotes() + "\n" : "") + "Refund reason: " + reason);
        }

        // Update cash drawer for refund if original was cash
        if (sale.getPaymentType() == PaymentType.CASH) {
            updateCashDrawerForSale(sale.getGymId(), refundAmount, true);
        }

        Sale savedSale = saleRepository.save(sale);
        log.info("Refunded {} for sale: {}", refundAmount, savedSale.getSaleNumber());

        return savedSale;
    }

    /**
     * Add item to an existing pending sale.
     */
    @Transactional
    public Sale addItemToSale(UUID saleId, SaleItemRequest itemRequest) {
        Sale sale = getSaleById(saleId);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new IllegalStateException("Cannot add items to a non-pending sale");
        }

        SaleItem item = createSaleItem(itemRequest, sale);
        sale.addItem(item);

        return saleRepository.save(sale);
    }

    /**
     * Remove item from an existing pending sale.
     */
    @Transactional
    public Sale removeItemFromSale(UUID saleId, UUID itemId) {
        Sale sale = getSaleById(saleId);

        if (sale.getStatus() != SaleStatus.PENDING) {
            throw new IllegalStateException("Cannot remove items from a non-pending sale");
        }

        SaleItem item = sale.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("SaleItem", itemId.toString()));

        sale.removeItem(item);

        return saleRepository.save(sale);
    }

    // ===== QUERY OPERATIONS =====

    public Sale getSaleById(UUID id) {
        return saleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sale", id.toString()));
    }

    public Optional<Sale> getSaleBySaleNumber(String saleNumber) {
        return saleRepository.findBySaleNumber(saleNumber);
    }

    public List<Sale> getSalesByGym(UUID gymId) {
        return saleRepository.findByGymId(gymId);
    }

    public List<Sale> getSalesByGymAndStatus(UUID gymId, SaleStatus status) {
        return saleRepository.findByGymIdAndStatus(gymId, status);
    }

    public List<Sale> getTodaysSales(UUID gymId) {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        return saleRepository.findTodaysSalesByGymId(gymId, startOfDay);
    }

    public List<Sale> getSalesByDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return saleRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
    }

    public List<Sale> getSalesByMember(UUID memberId) {
        return saleRepository.findByMemberId(memberId);
    }

    public List<Sale> getSalesByStaff(UUID staffId) {
        return saleRepository.findByStaffId(staffId);
    }

    // ===== CASH DRAWER OPERATIONS =====

    /**
     * Open a new cash drawer session.
     */
    @Transactional
    public CashDrawer openCashDrawer(UUID gymId, UUID staffId, BigDecimal openingBalance, String notes) {
        // Check if there's already an open drawer
        Optional<CashDrawer> existingOpen = cashDrawerRepository.findOpenDrawerByGymId(gymId);
        if (existingOpen.isPresent()) {
            throw new IllegalStateException("There is already an open cash drawer for this gym");
        }

        CashDrawer drawer = CashDrawer.builder()
                .sessionDate(LocalDate.now())
                .openedBy(staffId)
                .openingBalance(openingBalance)
                .notes(notes)
                .open(true)
                .openedAt(LocalDateTime.now())
                .build();
        drawer.setGymId(gymId);

        CashDrawer savedDrawer = cashDrawerRepository.save(drawer);
        log.info("Opened cash drawer for gym: {} by staff: {}", gymId, staffId);

        return savedDrawer;
    }

    /**
     * Close an open cash drawer session.
     */
    @Transactional
    public CashDrawer closeCashDrawer(UUID drawerId, UUID staffId, BigDecimal closingBalance, String closingNotes) {
        CashDrawer drawer = cashDrawerRepository.findById(drawerId)
                .orElseThrow(() -> new ResourceNotFoundException("CashDrawer", drawerId.toString()));

        if (!drawer.isOpen()) {
            throw new IllegalStateException("Cash drawer is already closed");
        }

        drawer.close(staffId, closingBalance, closingNotes);

        CashDrawer savedDrawer = cashDrawerRepository.save(drawer);
        log.info("Closed cash drawer: {} with variance: {}", drawerId, savedDrawer.getVariance());

        return savedDrawer;
    }

    /**
     * Get the current open cash drawer for a gym.
     */
    public Optional<CashDrawer> getOpenCashDrawer(UUID gymId) {
        return cashDrawerRepository.findOpenDrawerByGymId(gymId);
    }

    /**
     * Get cash drawer history for a gym.
     */
    public List<CashDrawer> getCashDrawerHistory(UUID gymId, LocalDate startDate, LocalDate endDate) {
        return cashDrawerRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
    }

    // ===== REPORTING =====

    /**
     * Get sales summary for a gym and date range.
     */
    public PosSalesSummary getSalesSummary(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        List<Sale> sales = saleRepository.findByGymIdAndDateRange(gymId, startDate, endDate);

        BigDecimal totalRevenue = BigDecimal.ZERO;
        BigDecimal totalRefunds = BigDecimal.ZERO;
        BigDecimal totalDiscounts = BigDecimal.ZERO;
        BigDecimal totalTax = BigDecimal.ZERO;
        int completedCount = 0;
        int refundedCount = 0;
        int cancelledCount = 0;
        int totalItemsSold = 0;

        for (Sale sale : sales) {
            if (sale.getStatus() == SaleStatus.COMPLETED) {
                completedCount++;
                totalRevenue = totalRevenue.add(sale.getTotalAmount());
                totalItemsSold += sale.getTotalItemCount();
            } else if (sale.getStatus() == SaleStatus.REFUNDED || sale.getStatus() == SaleStatus.PARTIALLY_REFUNDED) {
                refundedCount++;
                if (sale.getRefundedAmount() != null) {
                    totalRefunds = totalRefunds.add(sale.getRefundedAmount());
                }
            } else if (sale.getStatus() == SaleStatus.CANCELLED) {
                cancelledCount++;
            }

            if (sale.getDiscountAmount() != null) {
                totalDiscounts = totalDiscounts.add(sale.getDiscountAmount());
            }
            if (sale.getTaxAmount() != null) {
                totalTax = totalTax.add(sale.getTaxAmount());
            }
        }

        return new PosSalesSummary(
                sales.size(),
                completedCount,
                refundedCount,
                cancelledCount,
                totalItemsSold,
                totalRevenue,
                totalRefunds,
                totalRevenue.subtract(totalRefunds),
                totalDiscounts,
                totalTax);
    }

    /**
     * Get top selling items for a gym and date range.
     */
    public List<Object[]> getTopSellingItems(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return saleItemRepository.findTopSellingItems(gymId, startDate, endDate);
    }

    // ===== HELPER METHODS =====

    private String generateSaleNumber() {
        String datePrefix = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long counter = saleCounter.incrementAndGet();
        return "POS_SALE-" + datePrefix + "-" + String.format("%06d", counter % 1000000);
    }

    private SaleItem createSaleItem(SaleItemRequest request, Sale sale) {
        SaleItem item = SaleItem.builder()
                .inventoryItemId(request.inventoryItemId())
                .itemName(request.itemName())
                .itemSku(request.itemSku())
                .itemBarcode(request.itemBarcode())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .costPrice(request.costPrice())
                .discountPercentage(request.discountPercentage())
                .notes(request.notes())
                .build();

        // If we have an inventory item ID, get the details
        if (request.inventoryItemId() != null) {
            try {
                InventoryItem inventoryItem = inventoryService.getInventoryItemById(request.inventoryItemId());
                if (item.getItemSku() == null) {
                    item.setItemSku(inventoryItem.getSku());
                }
                if (item.getItemBarcode() == null) {
                    item.setItemBarcode(inventoryItem.getBarcode());
                }
                if (item.getCostPrice() == null) {
                    item.setCostPrice(inventoryItem.getUnitCost());
                }
            } catch (Exception e) {
                log.warn("Could not fetch inventory item details: {}", e.getMessage());
            }
        }

        item.calculateLineTotal();
        item.setOrganisationId(sale.getOrganisationId());
        item.setGymId(sale.getGymId());

        return item;
    }

    private void updateCashDrawerForSale(UUID gymId, BigDecimal amount, boolean isRefund) {
        Optional<CashDrawer> openDrawer = cashDrawerRepository.findOpenDrawerByGymId(gymId);
        if (openDrawer.isPresent()) {
            CashDrawer drawer = openDrawer.get();
            if (isRefund) {
                drawer.addRefund(amount);
            } else {
                drawer.addCashSale(amount);
            }
            cashDrawerRepository.save(drawer);
        }
    }

    // Summary record
    public record PosSalesSummary(
            int totalSales,
            int completedSales,
            int refundedSales,
            int cancelledSales,
            int totalItemsSold,
            BigDecimal totalRevenue,
            BigDecimal totalRefunds,
            BigDecimal netRevenue,
            BigDecimal totalDiscounts,
            BigDecimal totalTax) {
    }
}
