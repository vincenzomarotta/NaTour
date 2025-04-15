package com.example.natour.utils;

import static org.junit.Assert.*;

import com.example.natour.exceptions.PasswordNotCorrectException;

import org.junit.Before;
import org.junit.Test;

public class PasswordCheckerTest {

    PasswordChecker passwordChecker;
    String newPassword;
    String confirmPassword;

    @Before
    public void setUp() throws Exception {
        passwordChecker = new PasswordChecker();
    }

    @Test (expected = NullPointerException.class)
    public void checkPasswordNewPasswordNull() throws PasswordNotCorrectException {
        newPassword = null;
        confirmPassword = "CiaoCiao.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = NullPointerException.class)
    public void checkPasswordConfirmPasswordNull() throws PasswordNotCorrectException {
        confirmPassword = null;
        newPassword = "CiaoCiao.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = NullPointerException.class)
    public void checkPasswordBothPasswordNull() throws PasswordNotCorrectException {
        confirmPassword = null;
        newPassword = null;
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test
    public void checkPasswordCorrectPasswordSame() throws PasswordNotCorrectException {
        confirmPassword = "CiaoCiao.00";
        newPassword = "CiaoCiao.00";

        assertTrue (passwordChecker.checkPassword(newPassword, confirmPassword));
    }

    @Test
    public void checkPasswordCorrectPasswordNotSame() throws PasswordNotCorrectException {
        confirmPassword = "CiaoCiao.00";
        newPassword = "CiaoCiao.99";

        assertFalse (passwordChecker.checkPassword(newPassword, confirmPassword));
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordNewPasswordNotMatchingPattern() throws PasswordNotCorrectException {
        confirmPassword = "CiaoCiao.00";
        newPassword = "CiaoCiao00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordConfirmPasswordNotMatchingPattern() throws PasswordNotCorrectException {
        confirmPassword = "CiaoCiao00";
        newPassword = "CiaoCiao.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordBothNotMatchingPattern() throws PasswordNotCorrectException {
        confirmPassword = "CiaoCiao00";
        newPassword = "CiaoCiao00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordNotAcceptedLowercase() throws PasswordNotCorrectException {
        confirmPassword = "ciaociao.00";
        newPassword = "ciaociao.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordNotAcceptedUppercase() throws PasswordNotCorrectException {
        confirmPassword = "CIAOCIAO.00";
        newPassword = "CIAOCIAO.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordNoNumber() throws PasswordNotCorrectException {
        confirmPassword = "CiaoCiao.";
        newPassword = "CiaoCiao.";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordShort() throws PasswordNotCorrectException {
        confirmPassword = "Ciao.00";
        newPassword = "Ciao.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void checkPasswordLong() throws PasswordNotCorrectException {
        confirmPassword = " CiaoCiaoCiao.0000";
        newPassword = "CiaoCiaoCiao.0000";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

}