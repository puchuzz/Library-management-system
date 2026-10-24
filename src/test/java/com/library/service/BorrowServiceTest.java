package com.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.library.exception.BookNotAvailableException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.LibraryException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import com.library.model.MembershipType;
import com.library.model.Role;
import com.library.model.Staff;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BorrowServiceTest {
    private static final LocalDate START = LocalDate.of(2026, 3, 1);

    private InMemoryDaos.Books books;
    private InMemoryDaos.Members members;
    private InMemoryDaos.Borrows borrows;
    private Staff librarian;
    private Member student;

    private static Clock clockOn(LocalDate date) {
        return Clock.fixed(date.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    }

    private BorrowService serviceOn(LocalDate date) {
        return new BorrowService(books, members, borrows, new DailyFinePolicy(), clockOn(date));
    }

    private Book addBook(String isbn, int copies) {
        return books.save(new Book(0, isbn, "Book " + isbn, "Author", "Cat", copies, copies));
    }

    private Member addMember(String email, MembershipType type) {
        return members.save(Member.create(type, 0, "Member " + email, email, null, START, true));
    }

    @BeforeEach
    void setUp() {
        books = new InMemoryDaos.Books();
        members = new InMemoryDaos.Members();
        borrows = new InMemoryDaos.Borrows(books, members);
        librarian = Staff.create(Role.LIBRARIAN, 1, "Lib", "lib@x.com", null, "lib", "hash", true);
        student = addMember("s@x.com", MembershipType.STUDENT);
    }

    @Test
    void issueBookSetsDueDateFromMembershipAndReducesStock() {
        Book book = addBook("111", 2);
        BorrowRecord record = serviceOn(START).issueBook(librarian, book.getId(), student.getId());

        assertEquals(START.plusDays(14), record.getDueDate());
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void facultyGetsLongerLoanPeriod() {
        Member faculty = addMember("f@x.com", MembershipType.FACULTY);
        Book book = addBook("111", 1);
        BorrowRecord record = serviceOn(START).issueBook(librarian, book.getId(), faculty.getId());
        assertEquals(START.plusDays(30), record.getDueDate());
    }

    @Test
    void cannotIssueWhenNoCopiesLeft() {
        Book book = addBook("111", 1);
        Member other = addMember("o@x.com", MembershipType.STUDENT);
        BorrowService service = serviceOn(START);
        service.issueBook(librarian, book.getId(), student.getId());

        assertThrows(BookNotAvailableException.class,
                () -> service.issueBook(librarian, book.getId(), other.getId()));
    }

    @Test
    void studentCannotExceedBorrowLimit() {
        BorrowService service = serviceOn(START);
        for (int i = 0; i < 3; i++) {
            service.issueBook(librarian, addBook("isbn" + i, 1).getId(), student.getId());
        }
        Book fourth = addBook("isbn-4", 1);
        assertThrows(BorrowLimitExceededException.class,
                () -> service.issueBook(librarian, fourth.getId(), student.getId()));
    }

    @Test
    void sameBookCannotBeBorrowedTwiceByOneMember() {
        Book book = addBook("111", 3);
        BorrowService service = serviceOn(START);
        service.issueBook(librarian, book.getId(), student.getId());
        assertThrows(LibraryException.class, () -> service.issueBook(librarian, book.getId(), student.getId()));
    }

    @Test
    void inactiveMemberCannotBorrow() {
        student.setActive(false);
        Book book = addBook("111", 1);
        assertThrows(LibraryException.class, () -> serviceOn(START).issueBook(librarian, book.getId(), student.getId()));
    }

    @Test
    void memberWithOverdueBookCannotBorrowMore() {
        Book first = addBook("111", 1);
        Book second = addBook("222", 1);
        serviceOn(START).issueBook(librarian, first.getId(), student.getId());

        BorrowService later = serviceOn(START.plusDays(20)); // due date was START + 14
        assertThrows(LibraryException.class, () -> later.issueBook(librarian, second.getId(), student.getId()));
    }

    @Test
    void lateReturnChargesFineAndRestoresStock() {
        Book book = addBook("111", 1);
        BorrowRecord record = serviceOn(START).issueBook(librarian, book.getId(), student.getId());

        // due on START + 14; returned 5 days late at Rs. 2 per day
        BorrowRecord returned = serviceOn(START.plusDays(19)).returnBook(librarian, record.getId());

        assertEquals(0, new BigDecimal("10.00").compareTo(returned.getFineAmount()));
        assertTrue(returned.isReturned());
        assertEquals(1, book.getAvailableCopies());
    }

    @Test
    void onTimeReturnHasNoFine() {
        Book book = addBook("111", 1);
        BorrowRecord record = serviceOn(START).issueBook(librarian, book.getId(), student.getId());
        BorrowRecord returned = serviceOn(START.plusDays(14)).returnBook(librarian, record.getId());
        assertEquals(0, BigDecimal.ZERO.compareTo(returned.getFineAmount()));
    }

    @Test
    void returningTwiceIsRejected() {
        Book book = addBook("111", 1);
        BorrowService service = serviceOn(START);
        BorrowRecord record = service.issueBook(librarian, book.getId(), student.getId());
        service.returnBook(librarian, record.getId());
        assertThrows(LibraryException.class, () -> service.returnBook(librarian, record.getId()));
    }
}
