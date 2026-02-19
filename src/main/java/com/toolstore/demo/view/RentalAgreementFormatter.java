package com.toolstore.demo.view;

import com.toolstore.demo.model.RentalAgreement;
import org.springframework.stereotype.Component;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

// Displays a rental agreement, mainly for console output
// Can easily be extended for multiple formats
@Component
public class RentalAgreementFormatter {
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("MM/dd/yy");
    private static final NumberFormat CURRENCY_FMT = NumberFormat.getCurrencyInstance(Locale.US);

    // Format given by specification
    public static String format(RentalAgreement a) {
        return String.join("\n",
                "Tool code: " + a.getToolCode(),
                "Tool type: " + a.getToolType(),
                "Tool brand: " + a.getToolBrand(),
                "Rental days: " + a.getRentalDays(),
                "Check out date: " + a.getCheckoutDate().format(DATE_FMT),
                "Due date: " + a.getDueDate().format(DATE_FMT),
                "Daily rental charge: " + CURRENCY_FMT.format(a.getDailyRentalCharge()),
                "Charge days: " + a.getChargeDays(),
                "Pre-discount charge: " + CURRENCY_FMT.format(a.getPreDiscountCharge()),
                "Discount percent: " + a.getDiscountPercent() + "%",
                "Discount amount: " + CURRENCY_FMT.format(a.getDiscountAmount()),
                "Final charge: " + CURRENCY_FMT.format(a.getFinalCharge())
        );
    }
}