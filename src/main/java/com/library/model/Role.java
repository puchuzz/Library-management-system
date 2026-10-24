package com.library.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/** Staff roles and the permissions each one grants. */
public enum Role {
    ADMIN(EnumSet.allOf(Permission.class)),
    LIBRARIAN(EnumSet.of(
            Permission.VIEW_CATALOG,
            Permission.MANAGE_BOOKS,
            Permission.MANAGE_MEMBERS,
            Permission.ISSUE_RETURN,
            Permission.VIEW_REPORTS));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = Collections.unmodifiableSet(permissions);
    }

    public boolean has(Permission permission) {
        return permissions.contains(permission);
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
