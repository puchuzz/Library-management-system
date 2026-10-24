package com.library.exception;

/** Thrown when a member tries to exceed their borrowing limit. */
public class BorrowLimitExceededException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public BorrowLimitExceededException(String message) {
        super(message);
    }
}
