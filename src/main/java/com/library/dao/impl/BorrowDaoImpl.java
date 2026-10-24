package com.library.dao.impl;

import com.library.config.DBConnection;
import com.library.dao.BorrowDao;
import com.library.exception.BookNotAvailableException;
import com.library.exception.DataAccessException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.BorrowRecord;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BorrowDaoImpl implements BorrowDao {
    private static final String SELECT =
            "SELECT r.record_id, r.book_id, r.member_id, r.issued_by, r.borrow_date, r.due_date, "
                    + "r.return_date, r.fine_amount, b.title AS book_title, m.full_name AS member_name "
                    + "FROM borrow_records r "
                    + "JOIN books b ON b.book_id = r.book_id "
                    + "JOIN members m ON m.member_id = r.member_id ";

    @FunctionalInterface
    private interface Binder {
        void bind(PreparedStatement ps) throws SQLException;
    }

    /**
     * Stock decrement and loan insert run in one transaction. The UPDATE only succeeds while
     * available_copies > 0, so two librarians cannot issue the last copy twice.
     */
    @Override
    public BorrowRecord issue(int bookId, int memberId, int issuedBy, LocalDate borrowDate, LocalDate dueDate) {
        String decrement = "UPDATE books SET available_copies = available_copies - 1 "
                + "WHERE book_id = ? AND available_copies > 0";
        String insert = "INSERT INTO borrow_records (book_id, member_id, issued_by, borrow_date, due_date) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(decrement)) {
                    ps.setInt(1, bookId);
                    if (ps.executeUpdate() == 0) {
                        throw new BookNotAvailableException("No copies available for book id " + bookId);
                    }
                }
                int recordId;
                try (PreparedStatement ps = con.prepareStatement(insert, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setInt(1, bookId);
                    ps.setInt(2, memberId);
                    ps.setInt(3, issuedBy);
                    ps.setDate(4, Date.valueOf(borrowDate));
                    ps.setDate(5, Date.valueOf(dueDate));
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        keys.next();
                        recordId = keys.getInt(1);
                    }
                }
                con.commit();
                return findById(recordId).orElseThrow(() ->
                        new DataAccessException("Loan was saved but could not be reloaded", null));
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to issue book", e);
        }
    }

    @Override
    public BorrowRecord closeRecord(int recordId, LocalDate returnedOn, BigDecimal fine) {
        String close = "UPDATE borrow_records SET return_date = ?, fine_amount = ? "
                + "WHERE record_id = ? AND return_date IS NULL";
        String restock = "UPDATE books b JOIN borrow_records r ON r.book_id = b.book_id "
                + "SET b.available_copies = b.available_copies + 1 WHERE r.record_id = ?";
        try (Connection con = DBConnection.getConnection()) {
            con.setAutoCommit(false);
            try {
                try (PreparedStatement ps = con.prepareStatement(close)) {
                    ps.setDate(1, Date.valueOf(returnedOn));
                    ps.setBigDecimal(2, fine);
                    ps.setInt(3, recordId);
                    if (ps.executeUpdate() == 0) {
                        throw new ResourceNotFoundException("No open loan found with id " + recordId);
                    }
                }
                try (PreparedStatement ps = con.prepareStatement(restock)) {
                    ps.setInt(1, recordId);
                    ps.executeUpdate();
                }
                con.commit();
            } catch (SQLException | RuntimeException e) {
                con.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to return book", e);
        }
        return findById(recordId).orElseThrow(() -> new ResourceNotFoundException("Loan not found: " + recordId));
    }

    @Override
    public Optional<BorrowRecord> findById(int recordId) {
        List<BorrowRecord> list = query(SELECT + "WHERE r.record_id = ?", ps -> ps.setInt(1, recordId));
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    @Override
    public List<BorrowRecord> findActiveByMember(int memberId) {
        return query(SELECT + "WHERE r.member_id = ? AND r.return_date IS NULL ORDER BY r.due_date",
                ps -> ps.setInt(1, memberId));
    }

    @Override
    public List<BorrowRecord> findByMember(int memberId) {
        return query(SELECT + "WHERE r.member_id = ? ORDER BY r.borrow_date DESC, r.record_id DESC",
                ps -> ps.setInt(1, memberId));
    }

    @Override
    public List<BorrowRecord> findAllActive() {
        return query(SELECT + "WHERE r.return_date IS NULL ORDER BY r.due_date", ps -> { });
    }

    @Override
    public List<BorrowRecord> findOverdue(LocalDate today) {
        return query(SELECT + "WHERE r.return_date IS NULL AND r.due_date < ? ORDER BY r.due_date",
                ps -> ps.setDate(1, Date.valueOf(today)));
    }

    private List<BorrowRecord> query(String sql, Binder binder) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            binder.bind(ps);
            try (ResultSet rs = ps.executeQuery()) {
                List<BorrowRecord> records = new ArrayList<>();
                while (rs.next()) {
                    records.add(map(rs));
                }
                return records;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query loans", e);
        }
    }

    private BorrowRecord map(ResultSet rs) throws SQLException {
        Date returned = rs.getDate("return_date");
        BorrowRecord record = new BorrowRecord(
                rs.getInt("record_id"), rs.getInt("book_id"), rs.getInt("member_id"), rs.getInt("issued_by"),
                rs.getDate("borrow_date").toLocalDate(), rs.getDate("due_date").toLocalDate(),
                returned == null ? null : returned.toLocalDate(), rs.getBigDecimal("fine_amount"));
        record.setBookTitle(rs.getString("book_title"));
        record.setMemberName(rs.getString("member_name"));
        return record;
    }
}
