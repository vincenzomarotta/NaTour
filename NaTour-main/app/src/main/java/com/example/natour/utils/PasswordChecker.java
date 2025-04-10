package com.example.natour.utils;

import com.example.natour.exceptions.PasswordNotCorrectException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PasswordChecker {

    private final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()._–[{}]:;',?/*~$^+=<>]).{8,16}$";


   public boolean checkPassword(String newPassword, String confirmPassword) throws PasswordNotCorrectException {

       if(confirmPassword == null || newPassword == null)
           throw new NullPointerException();

       final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);

       Matcher matcher = pattern.matcher(newPassword);
       if(!matcher.matches()){
           throw new PasswordNotCorrectException();
       }

       matcher = pattern.matcher(confirmPassword);
       if(!matcher.matches()){
           throw new PasswordNotCorrectException();
       }

       return newPassword.equals(confirmPassword);
   }
}
