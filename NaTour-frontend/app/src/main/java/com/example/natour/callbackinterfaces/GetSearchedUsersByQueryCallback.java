package com.example.natour.callbackinterfaces;

import com.example.natour.entity.User;

import java.util.ArrayList;

public interface GetSearchedUsersByQueryCallback {
    void onSuccess(ArrayList<User> users);
    void onFailure();
    void onResultNotFound();
}
