package com.example.natour.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.natour.entity.User;

public class UserDataPreferences {
    private Context context;
    SharedPreferences sharedPreferences;

    private final String NAME = "name";
    private final String SURNAME = "surname";
    private final String PASSWORD = "password";
    private final String EMAIL = "email";
    private final String ADMIN = "admin";

    /**
     * Creates UserDataPreferences to store non-private user data.
     * @param context context of the application used to create SharedPreferences.
     */
    public UserDataPreferences(Context context){
        this.context = context;
        sharedPreferences = context.getSharedPreferences("user_data", context.MODE_PRIVATE);
    }

    /**
     * Sets user data.
     * @param user user datas gotten during registration or thanks to UserDAO method.
     */
    public void setUserData(User user){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(NAME, user.getName());
        editor.putString(SURNAME, user.getSurname());
        editor.putString(EMAIL, user.getEmail());
        editor.putBoolean(ADMIN, user.isAdmin());
        editor.apply();
    }

    /**
     * Checks if the current user is admin.
     * @return boolean.
     */
    public boolean checkUserIsAdmin(){
        return sharedPreferences.getBoolean(ADMIN,false);
    }

    /**
     * Gets current user name.
     * @return name string.
     */
    public String getUserName(){
        return sharedPreferences.getString(NAME, "");
    }

    /**
     * Gets current user surname.
     * @return surname string.
     */
    public String getUserSurname(){
        return sharedPreferences.getString(SURNAME, "");
    }

    /**
     * Gets current user email.
     * @return email string.
     */
    public String getUserEmail(){
        return sharedPreferences.getString(EMAIL, "");
    }

    /**
     * Sets current admin to true.
     */
    public void setUserAdminTrue(){
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(ADMIN, true);
        editor.apply();
    }


}
