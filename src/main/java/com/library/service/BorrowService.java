package com.library.service;

import com.library.dao.BookDao;
import com.library.dao.BorrowDao;
import com.library.dao.MemberDao;
import com.library.exception.BookNotAvailableException;
import com.library.exception.BorrowLimitExceededException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.BorrowRecord;
import com.library.model.Member;
import com.library.model.Permission;
import com.library.model.Staff;
import com.library.security.AccessControl;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

/** Circulation: issuing books, returning them and calculating fines. */
public class BorrowService {
    private final BookDao bookDao;
    private final MemberDao memberDao;
    private final BorrowDao borrowDao;
    private final FinePolicy finePolicy;
    private final Clock clock;

    public BorrowService(BookDao bookDao, MemberDao memberDao, BorrowDao borrowDao,
                         FinePolicy finePolicy, Clock clock) {
        this.bookDao = bookDao;
        this.memberDao = memberDao;
        this.borrowDao = borrowDao;
        this.finePolicy = finePolicy;
        this.clock = clock;
    }

    public BorrowRecord issueBook(Staff actor, int bookId, int memberId) {
        AccessControl.require(actor, Permission.ISSUE_RETURN);

        Member member = memberDao.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
        if (!member.isActive()) {
            throw new LibraryException("Member account is inactive: " + member.getFullName());
        }
        Book book = bookDao.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("No copies available for '" + book.getTitle() + "'");
        }

        LocalDate today = LocalDate.now(clock);
        List<BorrowRecord> active = borrowDao.findActiveByMember(memberId);
        if (active.stream().anyMatch(r -> r.getBookId() == bookId)) {
            throw new LibraryException(member.getFullName() + " already has '" + book.getTitle() + "' on loan");
        }
        if (active.stream().anyMatch(r -> r.isOverdue(today))) {
            throw new LibraryException(member.getFullName() + " has overdue books; return them first");
        }
        if (active.size() >= member.getBorrowLimit()) {
            throw new BorrowLimitExceededException(member.getFullName() + " has reached the limit of "
                    + member.getBorrowLimit() + " books (" + member.getTypeLabel() + ")");
        }

        LocalDate dueDate = today.plusDays(member.getLoanPeriodDays());
        return borrowDao.issue(bookId, memberId, actor.getId(), today, dueDate);
    }

    public BorrowRecord returnBook(Staff actor, int recordId) {
        AccessControl.require(actor, Permission.ISSUE_RETURN);

        BorrowRecord record = borrowDao.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + recordId));
        if (record.isReturned()) {
            throw new LibraryException("This loan was already returned on " + record.getReturnDate());
        }
        Member member = memberDao.findById(record.getMemberId())
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + record.getMemberId()));

        LocalDate today = LocalDate.now(clock);
        BigDecimal fine = finePolicy.calculateFine(record.getDueDate(), today, member.getFinePerDay());
        return borrowDao.closeRecord(recordId, today, fine);
    }

    public List<BorrowRecord> activeLoans(Staff actor) {
        AccessControl.require(actor, Permission.VIEW_REPORTS);
        return borrowDao.findAllActive();
    }

    public List<BorrowRecord> overdueLoans(Staff actor) {
        AccessControl.require(actor, Permission.VIEW_REPORTS);
        return borrowDao.findOverdue(LocalDate.now(clock));
    }

    public List<BorrowRecord> memberHistory(Staff actor, int memberId) {
        AccessControl.require(actor, Permission.VIEW_REPORTS);
        memberDao.findById(memberId)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + memberId));
        return borrowDao.findByMember(memberId);
    }
}
