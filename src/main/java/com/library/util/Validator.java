package com.library.util;

import com.library.exception.LibraryException;
import java.util.regex.Pattern;

/** Small input-validation helpers shared by the service layer. */
public final class Validator {
    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern PHONE = Pattern.compile("^[0-9+\\- ]{7,15}$");

    private Validator() { }

    public static String requireNonBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new LibraryException(fieldName + " must not be empty");
        }
        return value.trim();
    }

    public static String requireEmail(String value) {
        String email = requireNonBlank(value, "Email");
        if (!EMAIL.matcher(email).matches()) {
            throw new LibraryException("Invalid email address: " + email);
        }
        return email.toLowerCase();
    }

    /** Phone is optional: blank becomes null, otherwise it must look like a phone number. */
    public static String optionalPhone(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String phone = value.trim();
        if (!PHONE.matcher(phone).matches()) {
            throw new LibraryException("Invalid phone number: " + phone);
        }
        return phone;
    }

    public static void requirePositive(int value, String fieldName) {
        if (value <= 0) {
            throw new LibraryException(fieldName + " must be greater than zero");
        }
    }

    public static void requirePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            throw new LibraryException("Password must be at least 8 characters long");
        }
    }
}
