package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Charges a fixed rate for every day late, up to a maximum cap. */
public class DailyFinePolicy implements FinePolicy {
    public static final BigDecimal DEFAULT_CAP = new BigDecimal("500.00");

    private final BigDecimal cap;

    public DailyFinePolicy() {
        this(DEFAULT_CAP);
    }

    public DailyFinePolicy(BigDecimal cap) {
        this.cap = cap;
    }

    @Override
    public BigDecimal calculateFine(LocalDate dueDate, LocalDate returnedOn, BigDecimal ratePerDay) {
        long daysLate = ChronoUnit.DAYS.between(dueDate, returnedOn);
        if (daysLate <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        BigDecimal fine = ratePerDay.multiply(BigDecimal.valueOf(daysLate)).setScale(2);
        return fine.min(cap);
    }
}
