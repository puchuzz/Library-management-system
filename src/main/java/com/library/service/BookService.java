package com.library.service;

import com.library.dao.BookDao;
import com.library.exception.DuplicateResourceException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Book;
import com.library.model.Permission;
import com.library.model.Staff;
import com.library.security.AccessControl;
import com.library.util.Validator;
import java.util.List;

/** Book inventory management. */
public class BookService {
    private final BookDao bookDao;

    public BookService(BookDao bookDao) {
        this.bookDao = bookDao;
    }

    public Book addBook(Staff actor, String isbn, String title, String author, String category, int copies) {
        AccessControl.require(actor, Permission.MANAGE_BOOKS);
        String cleanIsbn = Validator.requireNonBlank(isbn, "ISBN");
        String cleanTitle = Validator.requireNonBlank(title, "Title");
        String cleanAuthor = Validator.requireNonBlank(author, "Author");
        String cleanCategory = Validator.requireNonBlank(category, "Category");
        Validator.requirePositive(copies, "Number of copies");
        if (bookDao.findByIsbn(cleanIsbn).isPresent()) {
            throw new DuplicateResourceException("A book with ISBN " + cleanIsbn + " already exists");
        }
        return bookDao.save(new Book(0, cleanIsbn, cleanTitle, cleanAuthor, cleanCategory, copies, copies));
    }

    /** Updates the given fields; null or blank arguments leave the existing value unchanged. */
    public Book updateDetails(Staff actor, int bookId, String title, String author, String category) {
        AccessControl.require(actor, Permission.MANAGE_BOOKS);
        Book book = getBook(bookId);
        if (title != null && !title.isBlank()) {
            book.setTitle(title.trim());
        }
        if (author != null && !author.isBlank()) {
            book.setAuthor(author.trim());
        }
        if (category != null && !category.isBlank()) {
            book.setCategory(category.trim());
        }
        bookDao.update(book);
        return book;
    }

    public Book addCopies(Staff actor, int bookId, int copies) {
        AccessControl.require(actor, Permission.MANAGE_BOOKS);
        Validator.requirePositive(copies, "Number of copies");
        Book book = getBook(bookId);
        book.addCopies(copies);
        bookDao.update(book);
        return book;
    }

    public void deleteBook(Staff actor, int bookId) {
        AccessControl.require(actor, Permission.DELETE_BOOKS);
        Book book = getBook(bookId);
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new LibraryException("Cannot delete: some copies are currently borrowed");
        }
        bookDao.deleteById(bookId);
    }

    public Book getBook(int bookId) {
        return bookDao.findById(bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + bookId));
    }

    public List<Book> search(Staff actor, String keyword) {
        AccessControl.require(actor, Permission.VIEW_CATALOG);
        return bookDao.search(Validator.requireNonBlank(keyword, "Search keyword"));
    }

    public List<Book> listAll(Staff actor) {
        AccessControl.require(actor, Permission.VIEW_CATALOG);
        return bookDao.findAll();
    }
}
