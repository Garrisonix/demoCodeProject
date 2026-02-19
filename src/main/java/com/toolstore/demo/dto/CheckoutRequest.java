package com.toolstore.demo.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record CheckoutRequest(@NotNull(message = "Tool code is required")
                              String toolCode,

                              @NotNull(message = "Rental day count is required")
                              @Min(value = 1, message = "Rental day count must be 1 or greater")
                              Integer rentalDayCount,

                              @NotNull(message = "Discount percent is required")
                              @Min(value = 0, message = "Discount percent must be between 0 and 100")
                              @Max(value = 100, message = "Discount percent must be between 0 and 100")
                              Integer discountPercent,

                              @JsonFormat(pattern = "MM/dd/yy")
                              @NotNull(message = "Checkout date is required")
                              LocalDate checkoutDate) {

    public CheckoutRequest(String toolCode, Integer rentalDayCount, Integer discountPercent, LocalDate checkoutDate) {
        this.toolCode = toolCode;
        this.rentalDayCount = rentalDayCount;
        this.discountPercent = discountPercent;
        this.checkoutDate = checkoutDate;
    }
}