package com.library.dao;

import com.library.model.BorrowRecord;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/** Persistence contract for loans. Issue and return are atomic (single transaction). */
public interface BorrowDao {
    /** Decrements stock and records the loan atomically. @throws com.library.exception.BookNotAvailableException */
    BorrowRecord issue(int bookId, int memberId, int issuedBy, LocalDate borrowDate, LocalDate dueDate);

    /** Marks the loan returned, stores the fine and restores stock atomically. */
    BorrowRecord closeRecord(int recordId, LocalDate returnedOn, BigDecimal fine);

    Optional<BorrowRecord> findById(int recordId);

    List<BorrowRecord> findActiveByMember(int memberId);

    List<BorrowRecord> findByMember(int memberId);

    List<BorrowRecord> findAllActive();

    List<BorrowRecord> findOverdue(LocalDate today);
}
