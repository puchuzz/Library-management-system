package com.library.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.library.exception.BookNotAvailableException;
import org.junit.jupiter.api.Test;

class BookTest {
    @Test
    void checkOutAndCheckInUpdateAvailability() {
        Book book = new Book(1, "111", "Clean Code", "Martin", "SE", 2, 2);
        book.checkOut();
        assertEquals(1, book.getAvailableCopies());
        book.checkOut();
        assertFalse(book.isAvailable());
        book.checkIn();
        assertTrue(book.isAvailable());
        assertEquals(2, book.getTotalCopies());
    }

    @Test
    void checkOutWithNoCopiesThrows() {
        Book book = new Book(1, "111", "Clean Code", "Martin", "SE", 1, 0);
        assertThrows(BookNotAvailableException.class, book::checkOut);
    }

    @Test
    void addCopiesIncreasesTotalAndAvailable() {
        Book book = new Book(1, "111", "Clean Code", "Martin", "SE", 2, 1);
        book.addCopies(3);
        assertEquals(5, book.getTotalCopies());
        assertEquals(4, book.getAvailableCopies());
    }

    @Test
    void invalidCopyCountsAreRejected() {
        assertThrows(IllegalArgumentException.class, () -> new Book(1, "1", "t", "a", "c", 1, 2));
    }
}
