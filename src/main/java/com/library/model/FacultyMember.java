package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class FacultyMember extends Member {
    public static final int BORROW_LIMIT = 10;
    public static final int LOAN_PERIOD_DAYS = 30;
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("1.00");

    public FacultyMember(int id, String fullName, String email, String phone, LocalDate joinedOn, boolean active) {
        super(id, fullName, email, phone, joinedOn, active);
    }

    @Override public MembershipType getMembershipType() { return MembershipType.FACULTY; }
    @Override public int getBorrowLimit() { return BORROW_LIMIT; }
    @Override public int getLoanPeriodDays() { return LOAN_PERIOD_DAYS; }
    @Override public BigDecimal getFinePerDay() { return FINE_PER_DAY; }
}
