package com.toolstore.demo.model;

import lombok.Getter;

import java.math.BigDecimal;

@Getter
public enum ToolType {
    LADDER(new BigDecimal("1.99"), true, true, false),
    CHAINSAW(new BigDecimal("1.49"), true, false, true),
    JACKHAMMER(new BigDecimal("2.99"), true, false, false);

    private final BigDecimal dailyCharge;
    private final boolean weekdayCharge;
    private final boolean weekendCharge;
    private final boolean holidayCharge;

    ToolType(BigDecimal dailyCharge, boolean weekdayCharge, boolean weekendCharge, boolean holidayCharge) {
        this.dailyCharge = dailyCharge;
        this.weekdayCharge = weekdayCharge;
        this.weekendCharge = weekendCharge;
        this.holidayCharge = holidayCharge;
    }

    // TODO: Encapsulate charging logic
    //public boolean isChargeableOn(boolean weekday, boolean holiday, boolean weekend) {
}