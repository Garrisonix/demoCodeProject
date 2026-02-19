package com.toolstore.demo.service;

import com.toolstore.demo.model.RentalAgreement;
import com.toolstore.demo.repository.ToolRepository;
import com.toolstore.demo.view.RentalAgreementFormatter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutServiceTest {

    private CheckoutService checkoutService;

    @BeforeEach
    void setUp() {
        ToolRepository toolRepository = new ToolRepository();
        ChargeService chargeService = new ChargeService();
        checkoutService = new CheckoutService(toolRepository, chargeService);
    }

    @Test
    void specification_test_1() {
        // Test 1: JAKR, 9/3/15, 5 days, 101% discount
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout(
                        "JAKR",
                        5,
                        101,
                        LocalDate.of(2015, 9, 3))
        );

        assertEquals("Discount percent must be between 0 and 100", exception.getMessage());
    }

    @Test
    void specification_test_2() {
        // Test 2: LADW, 7/2/20, 3 days, 10% discount
        // July 3rd 2020 is observed Independence Day (July 4th is Saturday)
        // Charge days: 7/3 (Fri-holiday, no charge), 7/4 (Sat, yes), 7/5 (Sun, yes) = 2 days
        RentalAgreement agreement = checkoutService.checkout(
                "LADW",
                3,
                10,
                LocalDate.of(2020, 7, 2)
        );

        assertEquals("LADW", agreement.getToolCode());
        assertEquals("LADDER", agreement.getToolType());
        assertEquals("Werner", agreement.getToolBrand());
        assertEquals(3, agreement.getRentalDays());
        assertEquals(LocalDate.of(2020, 7, 2), agreement.getCheckoutDate());
        assertEquals(LocalDate.of(2020, 7, 5), agreement.getDueDate());
        assertEquals(new BigDecimal("1.99"), agreement.getDailyRentalCharge());
        assertEquals(2, agreement.getChargeDays());
        assertEquals(new BigDecimal("3.98"), agreement.getPreDiscountCharge());
        assertEquals(10, agreement.getDiscountPercent());
        assertEquals(new BigDecimal("0.40"), agreement.getDiscountAmount());
        assertEquals(new BigDecimal("3.58"), agreement.getFinalCharge());

        // Print for verification
        System.out.println("\n=== Test 2 ===");
        System.out.println(RentalAgreementFormatter.format(agreement));
    }

    @Test
    void specification_test_3() {
        // Test 3: CHNS, 7/2/15, 5 days, 25% discount
        // July 3rd 2015 is observed Independence Day (July 4th is Saturday)
        // Chainsaw: weekday yes, weekend no, holiday yes
        // 7/3 (Fri-holiday, yes), 7/4 (Sat, no), 7/5 (Sun, no), 7/6 (Mon, yes), 7/7 (Tue, yes) = 3 days
        RentalAgreement agreement = checkoutService.checkout(
                "CHNS",
                5,
                25,
                LocalDate.of(2015, 7, 2)
        );

        assertEquals("CHNS", agreement.getToolCode());
        assertEquals("CHAINSAW", agreement.getToolType());
        assertEquals("Stihl", agreement.getToolBrand());
        assertEquals(5, agreement.getRentalDays());
        assertEquals(LocalDate.of(2015, 7, 2), agreement.getCheckoutDate());
        assertEquals(LocalDate.of(2015, 7, 7), agreement.getDueDate());
        assertEquals(new BigDecimal("1.49"), agreement.getDailyRentalCharge());
        assertEquals(3, agreement.getChargeDays());
        assertEquals(new BigDecimal("4.47"), agreement.getPreDiscountCharge());
        assertEquals(25, agreement.getDiscountPercent());
        assertEquals(new BigDecimal("1.12"), agreement.getDiscountAmount());
        assertEquals(new BigDecimal("3.35"), agreement.getFinalCharge());

        System.out.println("\n=== Test 3 ===");
        System.out.println(RentalAgreementFormatter.format(agreement));
    }

    @Test
    void specification_test_4() {
        // Test 4: JAKD, 9/3/15, 6 days, 0% discount
        // Labor Day 2015 is 9/7/15 (first Monday in September)
        // Jackhammer: weekday yes, weekend no, holiday no
        // 9/4 (Fri, yes), 9/5 (Sat, no), 9/6 (Sun, no), 9/7 (Mon-Labor Day, no), 9/8 (Tue, yes), 9/9 (Wed, yes) = 3 days
        RentalAgreement agreement = checkoutService.checkout(
                "JAKD",
                6,
                0,
                LocalDate.of(2015, 9, 3)
        );

        assertEquals("JAKD", agreement.getToolCode());
        assertEquals("JACKHAMMER", agreement.getToolType());
        assertEquals("DeWalt", agreement.getToolBrand());
        assertEquals(6, agreement.getRentalDays());
        assertEquals(LocalDate.of(2015, 9, 3), agreement.getCheckoutDate());
        assertEquals(LocalDate.of(2015, 9, 9), agreement.getDueDate());
        assertEquals(new BigDecimal("2.99"), agreement.getDailyRentalCharge());
        assertEquals(3, agreement.getChargeDays());
        assertEquals(new BigDecimal("8.97"), agreement.getPreDiscountCharge());
        assertEquals(0, agreement.getDiscountPercent());
        assertEquals(new BigDecimal("0.00"), agreement.getDiscountAmount());
        assertEquals(new BigDecimal("8.97"), agreement.getFinalCharge());

        System.out.println("\n=== Test 4 ===");
        System.out.println(RentalAgreementFormatter.format(agreement));
    }

    @Test
    void specification_test_5() {
        // Test 5: JAKR, 7/2/15, 9 days, 0% discount
        // July 3rd 2015 is observed Independence Day (July 4th is Saturday)
        // Jackhammer: weekday yes, weekend no, holiday no
        // 7/3 (Fri-holiday, no), 7/4 (Sat, no), 7/5 (Sun, no), 7/6-7/10 (Mon-Fri, 5 yes), 7/11 (Sat, no) = 5 days
        RentalAgreement agreement = checkoutService.checkout(
                "JAKR",
                9,
                0,
                LocalDate.of(2015, 7, 2)
        );

        assertEquals("JAKR", agreement.getToolCode());
        assertEquals("JACKHAMMER", agreement.getToolType());
        assertEquals("Ridgid", agreement.getToolBrand());
        assertEquals(9, agreement.getRentalDays());
        assertEquals(LocalDate.of(2015, 7, 2), agreement.getCheckoutDate());
        assertEquals(LocalDate.of(2015, 7, 11), agreement.getDueDate());
        assertEquals(new BigDecimal("2.99"), agreement.getDailyRentalCharge());
        assertEquals(5, agreement.getChargeDays());
        assertEquals(new BigDecimal("14.95"), agreement.getPreDiscountCharge());
        assertEquals(0, agreement.getDiscountPercent());
        assertEquals(new BigDecimal("0.00"), agreement.getDiscountAmount());
        assertEquals(new BigDecimal("14.95"), agreement.getFinalCharge());

        System.out.println("\n=== Test 5 ===");
        System.out.println(RentalAgreementFormatter.format(agreement));
    }

    @Test
    void specification_test_6() {
        // Test 6: JAKR, 7/2/20, 4 days, 50% discount
        // July 3rd 2020 is observed Independence Day (July 4th is Saturday)
        // Jackhammer: weekday yes, weekend no, holiday no
        // 7/3 (Fri-holiday, no), 7/4 (Sat, no), 7/5 (Sun, no), 7/6 (Mon, yes) = 1 day
        RentalAgreement agreement = checkoutService.checkout(
                "JAKR",
                4,
                50,
                LocalDate.of(2020, 7, 2)
        );

        assertEquals("JAKR", agreement.getToolCode());
        assertEquals("JACKHAMMER", agreement.getToolType());
        assertEquals("Ridgid", agreement.getToolBrand());
        assertEquals(4, agreement.getRentalDays());
        assertEquals(LocalDate.of(2020, 7, 2), agreement.getCheckoutDate());
        assertEquals(LocalDate.of(2020, 7, 6), agreement.getDueDate());
        assertEquals(new BigDecimal("2.99"), agreement.getDailyRentalCharge());
        assertEquals(1, agreement.getChargeDays());
        assertEquals(new BigDecimal("2.99"), agreement.getPreDiscountCharge());
        assertEquals(50, agreement.getDiscountPercent());
        assertEquals(new BigDecimal("1.50"), agreement.getDiscountAmount());
        assertEquals(new BigDecimal("1.49"), agreement.getFinalCharge());

        System.out.println("\n=== Test 6 ===");
        System.out.println(RentalAgreementFormatter.format(agreement));
    }

    @Test
    void testRentalDayCountLessThanOne_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout("LADW", 0, 10, LocalDate.of(2020, 7, 2))
        );

        assertEquals("Rental day count must be 1 or greater", exception.getMessage());
    }

    @Test
    void testNegativeDiscount_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout("LADW", 3, -1, LocalDate.of(2020, 7, 2))
        );

        assertEquals("Discount percent must be between 0 and 100", exception.getMessage());
    }

    @Test
    void testInvalidToolCode_ShouldThrowException() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> checkoutService.checkout("INVALID", 3, 10, LocalDate.of(2020, 7, 2))
        );

        assertEquals("Tool not found: INVALID", exception.getMessage());
    }
}