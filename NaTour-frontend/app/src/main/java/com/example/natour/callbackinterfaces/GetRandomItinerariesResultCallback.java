package com.example.natour.callbackinterfaces;

import com.example.natour.entity.SimpleItinerary;

import java.util.ArrayList;

public interface GetRandomItinerariesResultCallback {
    void onSuccess(ArrayList<SimpleItinerary> randomItineraries);
    void onFailure();
}
