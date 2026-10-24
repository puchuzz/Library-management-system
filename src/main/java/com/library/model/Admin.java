package com.library.model;

public class Admin extends Staff {
    public Admin(int id, String fullName, String email, String phone,
                 String username, String passwordHash, boolean active) {
        super(id, fullName, email, phone, username, passwordHash, active);
    }

    @Override
    public Role getRole() {
        return Role.ADMIN;
    }
}
