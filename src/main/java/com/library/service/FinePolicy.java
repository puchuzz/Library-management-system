package com.library.service;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Strategy for computing late-return fines (swap implementations without touching BorrowService). */
public interface FinePolicy {
    BigDecimal calculateFine(LocalDate dueDate, LocalDate returnedOn, BigDecimal ratePerDay);
}
