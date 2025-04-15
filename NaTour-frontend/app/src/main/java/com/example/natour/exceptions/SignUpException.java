package com.example.natour.exceptions;

public class SignUpException extends Exception{
    private static final String USER_NOT_CONFIRMED = "com.amazonaws.services.cognitoidentityprovider.model.UserNotFoundException";
    private static final String USERNAME_EXISTS = "com.amazonaws.services.cognitoidentityprovider.model.UsernameExistsException";
    private static final String NOT_AUTH = "com.amazonaws.services.cognitoidentityprovider.model.NotAuthorizedException"; //incorrect username or password
    private static final String INTERNAL_ERROR = "com.amazonaws.services.cognitoidentityprovider.model.InternalErrorException";
    private static final String INVALID_PASSWORD = "com.amazonaws.services.cognitoidentityprovider.model.InvalidPasswordException";
    private static final String CODE_MISMATCH = "com.amazonaws.services.cognitoidentityprovider.model.CodeMismatchException";
    private static final String CODE_DELIVERY = "com.amazonaws.services.cognitoidentityprovider.model.CodeDeliveryFailureException";
    private static final String ALIAS_EXISTS = "com.amazonaws.services.cognitoidentityprovider.model.AliasExistsException";

    /**
     * Gets the message based of the error.
     * @param canonicalName of the exception class
     * @return message
     */
    public static String getMessage(String canonicalName){
        switch (canonicalName){
            case USER_NOT_CONFIRMED:
                return null;
            case INVALID_PASSWORD:
                return "This password is not valid, please try something different.";
            case USERNAME_EXISTS:
            case ALIAS_EXISTS:
                return "Ops, it seems that someone already user this email, please choose a new one and try again.";
            case NOT_AUTH:
                return "Incorrect username or password, please try again.";
            case INTERNAL_ERROR:
                return "An internal error has occurred.\nTry again.";
            case CODE_MISMATCH:
                return "Incorrect code, please try again.";
            case CODE_DELIVERY:
                return "We couldn't send you the email with the code.\nPlease, try again.";
            default:
                return "Something went wrong. It's our fault!\nPlease, try again.";
        }
    }
}
