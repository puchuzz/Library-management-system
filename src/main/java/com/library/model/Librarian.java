package com.library.model;

public class Librarian extends Staff {
    public Librarian(int id, String fullName, String email, String phone,
                     String username, String passwordHash, boolean active) {
        super(id, fullName, email, phone, username, passwordHash, active);
    }

    @Override
    public Role getRole() {
        return Role.LIBRARIAN;
    }
}
