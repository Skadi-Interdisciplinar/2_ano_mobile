package com.example.mobile_skadi;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResetPasswordValidationUnitTest {

    @Test
    public void arePasswordFieldsFilled_allFilled_returnsTrue() {
        assertTrue(ResetPasswordActivity.arePasswordFieldsFilled("SenhaAtual1", "NovaSenha2", "NovaSenha2"));
    }

    @Test
    public void arePasswordFieldsFilled_anyBlankOrNull_returnsFalse() {
        assertFalse(ResetPasswordActivity.arePasswordFieldsFilled("", "NovaSenha2", "NovaSenha2"));
        assertFalse(ResetPasswordActivity.arePasswordFieldsFilled("SenhaAtual1", "  ", "NovaSenha2"));
        assertFalse(ResetPasswordActivity.arePasswordFieldsFilled("SenhaAtual1", "NovaSenha2", null));
    }

    @Test
    public void doPasswordsMatch_matchingPasswords_returnsTrue() {
        assertTrue(ResetPasswordActivity.doPasswordsMatch("SkadiNova123", "SkadiNova123"));
        assertTrue(ResetPasswordActivity.doPasswordsMatch(" SkadiNova123 ", "SkadiNova123"));
    }

    @Test
    public void doPasswordsMatch_differentPasswords_returnsFalse() {
        assertFalse(ResetPasswordActivity.doPasswordsMatch("SkadiNova123", "OutraSenha456"));
        assertFalse(ResetPasswordActivity.doPasswordsMatch(null, "SkadiNova123"));
    }
}
