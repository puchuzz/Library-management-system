package com.library.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class MemberPolymorphismTest {
    private final LocalDate today = LocalDate.of(2026, 1, 1);

    @Test
    void studentAndFacultyHaveDifferentRules() {
        Member student = Member.create(MembershipType.STUDENT, 1, "Riya", "r@x.com", null, today, true);
        Member faculty = Member.create(MembershipType.FACULTY, 2, "Meera", "m@x.com", null, today, true);

        assertTrue(student instanceof StudentMember);
        assertTrue(faculty instanceof FacultyMember);
        assertEquals(3, student.getBorrowLimit());
        assertEquals(10, faculty.getBorrowLimit());
        assertEquals(14, student.getLoanPeriodDays());
        assertEquals(30, faculty.getLoanPeriodDays());
        assertEquals(0, new BigDecimal("2.00").compareTo(student.getFinePerDay()));
        assertEquals(0, new BigDecimal("1.00").compareTo(faculty.getFinePerDay()));
    }

    @Test
    void staffRolesCarryDifferentPermissions() {
        Staff admin = Staff.create(Role.ADMIN, 1, "A", "a@x.com", null, "admin", "h", true);
        Staff librarian = Staff.create(Role.LIBRARIAN, 2, "L", "l@x.com", null, "lib", "h", true);

        assertTrue(admin instanceof Admin);
        assertTrue(librarian instanceof Librarian);
        assertTrue(admin.can(Permission.MANAGE_STAFF));
        assertTrue(admin.can(Permission.DELETE_BOOKS));
        assertTrue(librarian.can(Permission.ISSUE_RETURN));
        assertTrue(!librarian.can(Permission.MANAGE_STAFF));
        assertTrue(!librarian.can(Permission.DELETE_BOOKS));
    }
}
