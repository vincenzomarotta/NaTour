package com.example.natour.daointerfaces;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.natour.callbackinterfaces.GetSearchedUsersByQueryCallback;
import com.example.natour.callbackinterfaces.GetUserResultCallback;
import com.example.natour.callbackinterfaces.UpdateUserAdminResultCallback;
import com.example.natour.entity.User;

public interface UserDAO {
    void getUser(Context context, User user, GetUserResultCallback callback);
    void setAdminTrue(Context context, String email, UpdateUserAdminResultCallback callback);
    void getSearchedUsersByQuery(String search, Context context, @NonNull GetSearchedUsersByQueryCallback callback);
}
