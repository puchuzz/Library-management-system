package com.library.exception;

/** Thrown when login credentials are invalid or the account is disabled. */
public class AuthenticationFailedException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public AuthenticationFailedException(String message) {
        super(message);
    }
}
