package com.library.exception;

/** Thrown when the logged-in user's role lacks the required permission. */
public class PermissionDeniedException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public PermissionDeniedException(String message) {
        super(message);
    }
}
