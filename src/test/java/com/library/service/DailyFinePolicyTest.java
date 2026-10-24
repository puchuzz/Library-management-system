package com.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DailyFinePolicyTest {
    private final FinePolicy policy = new DailyFinePolicy();
    private final BigDecimal rate = new BigDecimal("2.00");
    private final LocalDate due = LocalDate.of(2026, 3, 15);

    @Test
    void noFineWhenReturnedOnOrBeforeDueDate() {
        assertEquals(0, BigDecimal.ZERO.compareTo(policy.calculateFine(due, due, rate)));
        assertEquals(0, BigDecimal.ZERO.compareTo(policy.calculateFine(due, due.minusDays(3), rate)));
    }

    @Test
    void fineIsRatePerLateDay() {
        assertEquals(0, new BigDecimal("10.00").compareTo(policy.calculateFine(due, due.plusDays(5), rate)));
    }

    @Test
    void fineIsCapped() {
        assertEquals(0, new BigDecimal("500.00").compareTo(policy.calculateFine(due, due.plusDays(1000), rate)));
    }
}
