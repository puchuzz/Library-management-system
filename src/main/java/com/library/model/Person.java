package com.library.model;

/** Abstract base for every human entity in the system (encapsulated state, abstract behaviour). */
public abstract class Person {
    private int id;
    private String fullName;
    private String email;
    private String phone;

    protected Person(int id, String fullName, String email, String phone) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
    }

    /** Human-readable label for the kind of person (e.g. STUDENT, LIBRARIAN). */
    public abstract String getTypeLabel();

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() {
        return String.format("%s[id=%d, name=%s, email=%s]", getTypeLabel(), id, fullName, email);
    }
}
