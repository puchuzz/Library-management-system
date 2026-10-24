package com.library.model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** One loan of one book to one member. */
public class BorrowRecord {
    private final int id;
    private final int bookId;
    private final int memberId;
    private final int issuedBy;
    private final LocalDate borrowDate;
    private final LocalDate dueDate;
    private LocalDate returnDate;
    private BigDecimal fineAmount;

    // Display-only fields filled by joined queries
    private String bookTitle;
    private String memberName;

    public BorrowRecord(int id, int bookId, int memberId, int issuedBy, LocalDate borrowDate,
                        LocalDate dueDate, LocalDate returnDate, BigDecimal fineAmount) {
        this.id = id;
        this.bookId = bookId;
        this.memberId = memberId;
        this.issuedBy = issuedBy;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.fineAmount = fineAmount == null ? BigDecimal.ZERO : fineAmount;
    }

    public boolean isReturned() {
        return returnDate != null;
    }

    public boolean isOverdue(LocalDate today) {
        return !isReturned() && today.isAfter(dueDate);
    }

    public int getId() { return id; }
    public int getBookId() { return bookId; }
    public int getMemberId() { return memberId; }
    public int getIssuedBy() { return issuedBy; }
    public LocalDate getBorrowDate() { return borrowDate; }
    public LocalDate getDueDate() { return dueDate; }
    public LocalDate getReturnDate() { return returnDate; }
    public void setReturnDate(LocalDate returnDate) { this.returnDate = returnDate; }
    public BigDecimal getFineAmount() { return fineAmount; }
    public void setFineAmount(BigDecimal fineAmount) { this.fineAmount = fineAmount; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }
    public String getMemberName() { return memberName; }
    public void setMemberName(String memberName) { this.memberName = memberName; }
}
