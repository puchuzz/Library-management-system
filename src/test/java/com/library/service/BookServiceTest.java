package com.library.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.library.exception.DuplicateResourceException;
import com.library.exception.PermissionDeniedException;
import com.library.model.Book;
import com.library.model.Role;
import com.library.model.Staff;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class BookServiceTest {
    private BookService service;
    private InMemoryDaos.Books books;
    private Staff admin;
    private Staff librarian;

    @BeforeEach
    void setUp() {
        books = new InMemoryDaos.Books();
        service = new BookService(books);
        admin = Staff.create(Role.ADMIN, 1, "Admin", "a@x.com", null, "admin", "h", true);
        librarian = Staff.create(Role.LIBRARIAN, 2, "Lib", "l@x.com", null, "lib", "h", true);
    }

    @Test
    void librarianCanAddBook() {
        Book book = service.addBook(librarian, "111", "Clean Code", "Martin", "SE", 3);
        assertEquals(3, book.getAvailableCopies());
        assertEquals(1, service.listAll(librarian).size());
    }

    @Test
    void duplicateIsbnIsRejected() {
        service.addBook(librarian, "111", "Clean Code", "Martin", "SE", 1);
        assertThrows(DuplicateResourceException.class,
                () -> service.addBook(librarian, "111", "Another", "Someone", "SE", 1));
    }

    @Test
    void onlyAdminCanDeleteBooks() {
        Book book = service.addBook(librarian, "111", "Clean Code", "Martin", "SE", 1);
        assertThrows(PermissionDeniedException.class, () -> service.deleteBook(librarian, book.getId()));
        service.deleteBook(admin, book.getId());
        assertEquals(0, service.listAll(admin).size());
    }

    @Test
    void anonymousUserIsDenied() {
        assertThrows(PermissionDeniedException.class, () -> service.listAll(null));
    }

    @Test
    void updateDetailsKeepsBlankFields() {
        Book book = service.addBook(librarian, "111", "Clean Code", "Martin", "SE", 1);
        service.updateDetails(librarian, book.getId(), "Clean Code 2nd Ed", "", null);
        assertEquals("Clean Code 2nd Ed", book.getTitle());
        assertEquals("Martin", book.getAuthor());
    }
}
