package com.library.exception;

/** Thrown when no copies of a book are available to borrow. */
public class BookNotAvailableException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public BookNotAvailableException(String message) {
        super(message);
    }
}
