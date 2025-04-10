package com.example.natour.utils;

import static org.junit.Assert.*;

import com.example.natour.exceptions.PasswordNotCorrectException;

import org.junit.Before;
import org.junit.Test;

public class PasswordCheckerTestWhite {

    PasswordChecker passwordChecker;
    String newPassword;
    String confirmPassword;

    @Before
    public void setUp() throws Exception {
        passwordChecker = new PasswordChecker();
    }

    @Test (expected = NullPointerException.class)
    public void testGenerateCheckPasswordWhiteBoxPath_NI_1_2_NF() throws PasswordNotCorrectException {
        newPassword = null;
        confirmPassword = "CiaoCiao.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void testGenerateCheckPasswordWhiteBoxPath_NI_1_3_4_5_6_NF() throws PasswordNotCorrectException {
        newPassword = "Ciao.0";
        confirmPassword = "CiaoCiao.00";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test (expected = PasswordNotCorrectException.class)
    public void testGenerateCheckPasswordWhiteBoxPath_NI_1_3_4_5_7_8_9_NF() throws PasswordNotCorrectException {
        newPassword = "CiaoCiao.00";
        confirmPassword = "Ciao.0";
        passwordChecker.checkPassword(newPassword, confirmPassword);
        fail();
    }

    @Test
    public void testGenerateCheckPasswordWhiteBoxPath_NI_1_3_4_5_7_8_10_NF() throws PasswordNotCorrectException {
        newPassword = "CiaoCiao.00";
        confirmPassword = "CiaoCiao.00";

        assertTrue (passwordChecker.checkPassword(newPassword, confirmPassword));

    }

}