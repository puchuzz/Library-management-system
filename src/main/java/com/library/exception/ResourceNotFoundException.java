package com.library.exception;

/** Thrown when a requested book, member, staff or loan does not exist. */
public class ResourceNotFoundException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
