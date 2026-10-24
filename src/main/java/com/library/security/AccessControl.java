package com.library.security;

import com.library.exception.PermissionDeniedException;
import com.library.model.Permission;

/** Central place for role-based authorization checks used by the service layer. */
public final class AccessControl {
    private AccessControl() { }

    public static void require(Authorizable actor, Permission permission) {
        if (actor == null) {
            throw new PermissionDeniedException("You must be logged in to do this");
        }
        if (!actor.can(permission)) {
            throw new PermissionDeniedException(
                    "Access denied: role " + actor.getRole() + " lacks permission " + permission);
        }
    }
}
