package com.toolstore.demo.service;

import com.toolstore.demo.model.ToolType;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.TemporalAdjusters;

@Service
public class ChargeService {

    public int calculateChargeDays(ToolType toolType, LocalDate startDate, LocalDate endDate) {
        int chargeDays = 0;
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            if (isChargeable(toolType, currentDate)) {
                chargeDays++;
            }
            currentDate = currentDate.plusDays(1);
        }

        return chargeDays;
    }

    private boolean isChargeable(ToolType toolType, LocalDate date) {
        boolean isWeekend = isWeekend(date);
        boolean isHoliday = isHoliday(date);

        if (isWeekend && !toolType.isWeekendCharge()) {
            return false;
        }

        if (isHoliday && !toolType.isHolidayCharge()) {
            return false;
        }

        return isWeekend || toolType.isWeekdayCharge();
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private boolean isHoliday(LocalDate date) {
        return isIndependenceDay(date) || isLaborDay(date);
    }

    private boolean isIndependenceDay(LocalDate date) {
        int year = date.getYear();
        LocalDate july4th = LocalDate.of(year, Month.JULY, 4);

        // If July 4th falls on Saturday, observed on Friday (July 3rd)
        if (july4th.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return date.equals(july4th.minusDays(1));
        }

        // If July 4th falls on Sunday, observed on Monday (July 5th)
        if (july4th.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return date.equals(july4th.plusDays(1));
        }

        // Otherwise, observed on July 4th itself
        return date.equals(july4th);
    }

    private boolean isLaborDay(LocalDate date) {
        // Labor Day is the first Monday in September
        int year = date.getYear();
        LocalDate firstMondayInSeptember = LocalDate.of(year, Month.SEPTEMBER, 1)
                .with(TemporalAdjusters.firstInMonth(DayOfWeek.MONDAY));

        return date.equals(firstMondayInSeptember);
    }
}