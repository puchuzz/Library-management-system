package com.library.service;

import com.library.dao.StaffDao;
import com.library.exception.AuthenticationFailedException;
import com.library.exception.DuplicateResourceException;
import com.library.exception.LibraryException;
import com.library.exception.ResourceNotFoundException;
import com.library.model.Permission;
import com.library.model.Role;
import com.library.model.Staff;
import com.library.security.AccessControl;
import com.library.security.PasswordUtil;
import com.library.util.Validator;
import java.util.List;

/** Authentication and staff-account management. */
public class AuthService {
    public static final String DEFAULT_ADMIN_USERNAME = "admin";
    public static final String DEFAULT_ADMIN_PASSWORD = "Admin@123";

    private final StaffDao staffDao;

    public AuthService(StaffDao staffDao) {
        this.staffDao = staffDao;
    }

    public Staff login(String username, String password) {
        Staff staff = staffDao.findByUsername(username == null ? "" : username.trim())
                .orElseThrow(() -> new AuthenticationFailedException("Invalid username or password"));
        if (!staff.isActive() || !PasswordUtil.verify(password, staff.getPasswordHash())) {
            throw new AuthenticationFailedException("Invalid username or password");
        }
        return staff;
    }

    /** Creates the first admin account if the staff table is empty. @return true if one was created */
    public boolean bootstrapDefaultAdmin() {
        if (staffDao.count() > 0) {
            return false;
        }
        Staff admin = Staff.create(Role.ADMIN, 0, "System Administrator", "admin@library.local", null,
                DEFAULT_ADMIN_USERNAME, PasswordUtil.hash(DEFAULT_ADMIN_PASSWORD), true);
        staffDao.save(admin);
        return true;
    }

    public Staff createStaff(Staff actor, Role role, String username, String password,
                             String fullName, String email, String phone) {
        AccessControl.require(actor, Permission.MANAGE_STAFF);
        String cleanUsername = Validator.requireNonBlank(username, "Username");
        Validator.requirePasswordStrength(password);
        String cleanName = Validator.requireNonBlank(fullName, "Full name");
        String cleanEmail = Validator.requireEmail(email);
        String cleanPhone = Validator.optionalPhone(phone);
        if (staffDao.findByUsername(cleanUsername).isPresent()) {
            throw new DuplicateResourceException("Username already taken: " + cleanUsername);
        }
        Staff staff = Staff.create(role, 0, cleanName, cleanEmail, cleanPhone,
                cleanUsername, PasswordUtil.hash(password), true);
        return staffDao.save(staff);
    }

    public List<Staff> listStaff(Staff actor) {
        AccessControl.require(actor, Permission.MANAGE_STAFF);
        return staffDao.findAll();
    }

    public void setStaffActive(Staff actor, int staffId, boolean active) {
        AccessControl.require(actor, Permission.MANAGE_STAFF);
        if (actor.getId() == staffId && !active) {
            throw new LibraryException("You cannot deactivate your own account");
        }
        Staff target = staffDao.findById(staffId)
                .orElseThrow(() -> new ResourceNotFoundException("Staff member not found: " + staffId));
        target.setActive(active);
        staffDao.update(target);
    }

    public void changePassword(Staff actor, String oldPassword, String newPassword) {
        if (!PasswordUtil.verify(oldPassword, actor.getPasswordHash())) {
            throw new AuthenticationFailedException("Current password is incorrect");
        }
        Validator.requirePasswordStrength(newPassword);
        actor.setPasswordHash(PasswordUtil.hash(newPassword));
        staffDao.update(actor);
    }
}
