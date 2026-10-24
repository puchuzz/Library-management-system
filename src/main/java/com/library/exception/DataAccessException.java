package com.library.exception;

/** Wraps checked {@link java.sql.SQLException}s so upper layers stay free of JDBC details. */
public class DataAccessException extends LibraryException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
