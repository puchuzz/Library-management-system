package com.library.dao.impl;

import com.library.config.DBConnection;
import com.library.dao.BookDao;
import com.library.exception.DataAccessException;
import com.library.exception.LibraryException;
import com.library.model.Book;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookDaoImpl implements BookDao {
    private static final String SELECT =
            "SELECT book_id, isbn, title, author, category, total_copies, available_copies FROM books";

    @Override
    public Book save(Book book) {
        String sql = "INSERT INTO books (isbn, title, author, category, total_copies, available_copies) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    book.setId(keys.getInt(1));
                }
            }
            return book;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to save book", e);
        }
    }

    @Override
    public Optional<Book> findById(Integer id) {
        return findOne(SELECT + " WHERE book_id = ?", id);
    }

    @Override
    public Optional<Book> findByIsbn(String isbn) {
        return findOne(SELECT + " WHERE isbn = ?", isbn);
    }

    @Override
    public List<Book> findAll() {
        return findMany(SELECT + " ORDER BY title");
    }

    @Override
    public List<Book> search(String keyword) {
        String like = "%" + keyword.toLowerCase() + "%";
        return findMany(SELECT + " WHERE LOWER(title) LIKE ? OR LOWER(author) LIKE ? "
                + "OR LOWER(category) LIKE ? OR LOWER(isbn) LIKE ? ORDER BY title", like, like, like, like);
    }

    @Override
    public boolean update(Book book) {
        String sql = "UPDATE books SET isbn = ?, title = ?, author = ?, category = ?, "
                + "total_copies = ?, available_copies = ? WHERE book_id = ?";
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, book.getIsbn());
            ps.setString(2, book.getTitle());
            ps.setString(3, book.getAuthor());
            ps.setString(4, book.getCategory());
            ps.setInt(5, book.getTotalCopies());
            ps.setInt(6, book.getAvailableCopies());
            ps.setInt(7, book.getId());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new DataAccessException("Failed to update book", e);
        }
    }

    @Override
    public boolean deleteById(Integer id) {
        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement("DELETE FROM books WHERE book_id = ?")) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new LibraryException("This book has borrow history and cannot be deleted");
        } catch (SQLException e) {
            throw new DataAccessException("Failed to delete book", e);
        }
    }

    private Optional<Book> findOne(String sql, Object... params) {
        List<Book> result = findMany(sql, params);
        return result.isEmpty() ? Optional.empty() : Optional.of(result.get(0));
    }

    private List<Book> findMany(String sql, Object... params) {
        try (Connection con = DBConnection.getConnection(); PreparedStatement ps = con.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Book> books = new ArrayList<>();
                while (rs.next()) {
                    books.add(map(rs));
                }
                return books;
            }
        } catch (SQLException e) {
            throw new DataAccessException("Failed to query books", e);
        }
    }

    private Book map(ResultSet rs) throws SQLException {
        return new Book(rs.getInt("book_id"), rs.getString("isbn"), rs.getString("title"),
                rs.getString("author"), rs.getString("category"),
                rs.getInt("total_copies"), rs.getInt("available_copies"));
    }
}
