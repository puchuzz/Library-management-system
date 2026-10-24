package com.library.model;

/** Contract for anything that can be checked out and returned (abstraction). */
public interface Borrowable {
    boolean isAvailable();

    /** Marks one copy as borrowed. @throws com.library.exception.BookNotAvailableException if none left */
    void checkOut();

    /** Marks one copy as returned. */
    void checkIn();
}
