package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * A library member. Borrowing rules differ per membership type, so they are
 * abstract here and supplied by subclasses (polymorphism).
 */
public abstract class Member extends Person {
    private final LocalDate joinedOn;
    private boolean active;

    protected Member(int id, String fullName, String email, String phone, LocalDate joinedOn, boolean active) {
        super(id, fullName, email, phone);
        this.joinedOn = joinedOn;
        this.active = active;
    }

    public abstract MembershipType getMembershipType();

    /** Maximum number of books this member may hold at once. */
    public abstract int getBorrowLimit();

    /** Number of days a book can be kept before it is overdue. */
    public abstract int getLoanPeriodDays();

    /** Fine charged for every day a book is returned late. */
    public abstract BigDecimal getFinePerDay();

    @Override
    public String getTypeLabel() {
        return getMembershipType().name();
    }

    public LocalDate getJoinedOn() { return joinedOn; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** Factory used by the DAO layer to build the right subclass. */
    public static Member create(MembershipType type, int id, String fullName, String email,
                                String phone, LocalDate joinedOn, boolean active) {
        return switch (type) {
            case STUDENT -> new StudentMember(id, fullName, email, phone, joinedOn, active);
            case FACULTY -> new FacultyMember(id, fullName, email, phone, joinedOn, active);
        };
    }
}
