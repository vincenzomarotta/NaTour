package com.example.natour.callbackinterfaces;

import com.example.natour.entity.User;

public interface GetUserResultCallback {
    void onSuccess(User user);
    void onFailure();
}
