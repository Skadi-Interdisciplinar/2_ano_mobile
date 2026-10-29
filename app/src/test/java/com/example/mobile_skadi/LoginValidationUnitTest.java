package com.example.mobile_skadi;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoginValidationUnitTest {

    @Test
    public void isUsernameValid_withValidUsername_returnsTrue() {
        assertTrue(LoginActivity.isUsernameValid("maria.rosa0912"));
        assertTrue(LoginActivity.isUsernameValid("  diego.valadares  "));
    }

    @Test
    public void isUsernameValid_withNullOrEmpty_returnsFalse() {
        assertFalse(LoginActivity.isUsernameValid(null));
        assertFalse(LoginActivity.isUsernameValid(""));
        assertFalse(LoginActivity.isUsernameValid("   "));
    }

    @Test
    public void isPasswordValid_withValidPassword_returnsTrue() {
        assertTrue(LoginActivity.isPasswordValid("Skadi@2026"));
        assertTrue(LoginActivity.isPasswordValid("123456"));
    }

    @Test
    public void isPasswordValid_withNullOrBlank_returnsFalse() {
        assertFalse(LoginActivity.isPasswordValid(null));
        assertFalse(LoginActivity.isPasswordValid(""));
        assertFalse(LoginActivity.isPasswordValid("    "));
    }
}
