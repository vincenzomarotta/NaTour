package com.example.natour.callbackinterfaces;

import com.example.natour.entity.SimpleMessage;

import java.util.ArrayList;

public interface GetUserChatsResultCallback {
    void onSuccess(ArrayList<SimpleMessage> userChats);
    void onFailure();
}
