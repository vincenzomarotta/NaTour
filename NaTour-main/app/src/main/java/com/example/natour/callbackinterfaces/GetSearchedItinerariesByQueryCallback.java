package com.example.natour.callbackinterfaces;

import com.example.natour.entity.SimpleItinerary;

import java.util.ArrayList;

public interface GetSearchedItinerariesByQueryCallback {
    void onSuccess(ArrayList<SimpleItinerary> simpleItineraries);
    void onFailure();
    void onResultNotFound();
}
