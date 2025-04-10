package com.example.natour.callbackinterfaces;

import com.example.natour.entity.SimpleItinerary;

import java.util.ArrayList;

public interface GetUserListCallback {
    void onSuccess(ArrayList<SimpleItinerary> toVisitList, ArrayList<SimpleItinerary> favoriteList, ArrayList<SimpleItinerary> myItineraryList);
    void onFailure();
}
