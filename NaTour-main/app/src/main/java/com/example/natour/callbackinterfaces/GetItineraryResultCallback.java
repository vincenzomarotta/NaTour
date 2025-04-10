package com.example.natour.callbackinterfaces;

import com.example.natour.entity.Itinerary;

public interface GetItineraryResultCallback {
    void onSuccess(Itinerary itinerary);
    void onFailure();
}
