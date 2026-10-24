package com.library.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class PasswordUtilTest {
    @Test
    void correctPasswordVerifies() {
        String hash = PasswordUtil.hash("Secret@123");
        assertTrue(PasswordUtil.verify("Secret@123", hash));
    }

    @Test
    void wrongPasswordFails() {
        String hash = PasswordUtil.hash("Secret@123");
        assertFalse(PasswordUtil.verify("secret@123", hash));
    }

    @Test
    void samePasswordProducesDifferentHashesBecauseOfSalt() {
        assertNotEquals(PasswordUtil.hash("Secret@123"), PasswordUtil.hash("Secret@123"));
    }

    @Test
    void malformedStoredValueIsRejected() {
        assertFalse(PasswordUtil.verify("x", "not-a-hash"));
        assertFalse(PasswordUtil.verify("x", null));
    }
}
