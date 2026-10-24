package com.library.model;

import com.library.exception.BookNotAvailableException;

public class Book extends LibraryItem implements Borrowable {
    private String isbn;
    private String author;
    private int totalCopies;
    private int availableCopies;

    public Book(int id, String isbn, String title, String author, String category,
                int totalCopies, int availableCopies) {
        super(id, title, category);
        if (totalCopies < 0 || availableCopies < 0 || availableCopies > totalCopies) {
            throw new IllegalArgumentException("Invalid copy counts: total=" + totalCopies
                    + ", available=" + availableCopies);
        }
        this.isbn = isbn;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = availableCopies;
    }

    @Override
    public String describe() {
        return String.format("%s by %s (ISBN %s) - %d/%d available",
                getTitle(), author, isbn, availableCopies, totalCopies);
    }

    @Override
    public boolean isAvailable() {
        return availableCopies > 0;
    }

    @Override
    public void checkOut() {
        if (!isAvailable()) {
            throw new BookNotAvailableException("No copies available for '" + getTitle() + "'");
        }
        availableCopies--;
    }

    @Override
    public void checkIn() {
        if (availableCopies >= totalCopies) {
            throw new IllegalStateException("All copies of '" + getTitle() + "' are already in the library");
        }
        availableCopies++;
    }

    /** Adds copies to the inventory (both total and available). */
    public void addCopies(int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Copies to add must be positive");
        }
        totalCopies += count;
        availableCopies += count;
    }

    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }

    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }

    public int getTotalCopies() { return totalCopies; }
    public int getAvailableCopies() { return availableCopies; }
}
