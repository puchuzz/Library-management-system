package com.library.model;

import com.library.security.Authorizable;

/** Abstract library staff account. Concrete roles decide their own {@link Role}. */
public abstract class Staff extends Person implements Authorizable {
    private final String username;
    private String passwordHash;
    private boolean active;

    protected Staff(int id, String fullName, String email, String phone,
                    String username, String passwordHash, boolean active) {
        super(id, fullName, email, phone);
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = active;
    }

    @Override
    public String getTypeLabel() {
        return getRole().name();
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    /** Factory used by the DAO / service layers to build the right subclass. */
    public static Staff create(Role role, int id, String fullName, String email, String phone,
                               String username, String passwordHash, boolean active) {
        return switch (role) {
            case ADMIN -> new Admin(id, fullName, email, phone, username, passwordHash, active);
            case LIBRARIAN -> new Librarian(id, fullName, email, phone, username, passwordHash, active);
        };
    }
}
