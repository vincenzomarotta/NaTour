package com.example.natour.callbackinterfaces;

public interface SaveItineraryResultCallback {
    void onSuccess(int newId);
    void onFailure();
}
