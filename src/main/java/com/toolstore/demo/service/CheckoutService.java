package com.toolstore.demo.service;

import com.toolstore.demo.model.RentalAgreement;
import com.toolstore.demo.model.Tool;
import com.toolstore.demo.repository.ToolRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
public class CheckoutService {

    private final ToolRepository toolRepository;
    private final ChargeService chargeService;

    public CheckoutService(ToolRepository toolRepository, ChargeService chargeService) {
        this.toolRepository = toolRepository;
        this.chargeService = chargeService;
    }

    public RentalAgreement checkout(String toolCode, int rentalDayCount, int discountPercent, LocalDate checkoutDate) {
        // Validation
        validateRentalDayCount(rentalDayCount);
        validateDiscountPercent(discountPercent);

        // Get tool
        Tool tool = toolRepository.findByCode(toolCode)
                .orElseThrow(() -> new IllegalArgumentException("Tool not found: " + toolCode));

        // Calculate due date
        LocalDate dueDate = checkoutDate.plusDays(rentalDayCount);

        // Calculate charge days
        int chargeDays = chargeService.calculateChargeDays(
                tool.toolType(),
                checkoutDate.plusDays(1), // Start from day after checkout
                dueDate
        );

        // Calculate charges
        BigDecimal dailyCharge = tool.toolType().getDailyCharge();
        BigDecimal preDiscountCharge = dailyCharge
                .multiply(BigDecimal.valueOf(chargeDays))
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal discountAmount = preDiscountCharge
                .multiply(BigDecimal.valueOf(discountPercent))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

        BigDecimal finalCharge = preDiscountCharge.subtract(discountAmount);

        // Build rental agreement
        return RentalAgreement.builder()
                .toolCode(tool.code())
                .toolType(tool.toolType().name())
                .toolBrand(tool.brand())
                .rentalDays(rentalDayCount)
                .checkoutDate(checkoutDate)
                .dueDate(dueDate)
                .dailyRentalCharge(dailyCharge)
                .chargeDays(chargeDays)
                .preDiscountCharge(preDiscountCharge)
                .discountPercent(discountPercent)
                .discountAmount(discountAmount)
                .finalCharge(finalCharge)
                .build();
    }

    private void validateRentalDayCount(int rentalDayCount) {
        if (rentalDayCount < 1) {
            throw new IllegalArgumentException("Rental day count must be 1 or greater");
        }
    }

    private void validateDiscountPercent(int discountPercent) {
        if (discountPercent < 0 || discountPercent > 100) {
            throw new IllegalArgumentException("Discount percent must be between 0 and 100");
        }
    }
}