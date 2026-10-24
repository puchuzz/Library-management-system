package com.library.exception;

/** Thrown when a unique value (ISBN, email, username) already exists. */
public class DuplicateResourceException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public DuplicateResourceException(String message) {
        super(message);
    }
}
