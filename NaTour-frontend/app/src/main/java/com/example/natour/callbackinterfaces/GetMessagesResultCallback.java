package com.example.natour.callbackinterfaces;

import com.example.natour.entity.Message;

import java.util.ArrayList;

public interface GetMessagesResultCallback {
    void onSuccess(ArrayList<Message> users);
    void onFailure();
    void onResultNotFound();
}
