package com.library.security;

import com.library.model.Permission;
import com.library.model.Role;

/** Anything that carries a {@link Role} and can be checked against a {@link Permission}. */
public interface Authorizable {
    Role getRole();

    default boolean can(Permission permission) {
        return getRole().has(permission);
    }
}
