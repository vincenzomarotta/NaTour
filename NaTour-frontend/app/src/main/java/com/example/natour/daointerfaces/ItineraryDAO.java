package com.example.natour.daointerfaces;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.natour.callbackinterfaces.GetItineraryResultCallback;
import com.example.natour.callbackinterfaces.GetRandomItinerariesResultCallback;
import com.example.natour.callbackinterfaces.GetSearchedItinerariesByQueryCallback;
import com.example.natour.callbackinterfaces.GetUserListCallback;
import com.example.natour.callbackinterfaces.SaveItineraryResultCallback;
import com.example.natour.callbackinterfaces.SetFavoriteResultCallback;
import com.example.natour.callbackinterfaces.SetToVisitResultCallback;
import com.example.natour.callbackinterfaces.UpdateItineraryResultCallback;
import com.example.natour.entity.Itinerary;

import java.util.HashMap;

public interface ItineraryDAO {
    void saveItinerary(Itinerary itinerary, Context context, @NonNull SaveItineraryResultCallback callback);
    void updateItinerary(Itinerary itinerary, Context context, @NonNull UpdateItineraryResultCallback callback);
    void getItinerary(int id, Context context, @NonNull GetItineraryResultCallback callback);
    void setFavorite(int id, boolean value, Context context, @NonNull SetFavoriteResultCallback callback);
    void setToVisit(int id, boolean value, Context context, @NonNull SetToVisitResultCallback callback);
    void getSearchedItinerariesByQuery(String search, Context context, HashMap<String, String> filter,
                                       @NonNull GetSearchedItinerariesByQueryCallback callback);
    void getUserList(String email, Context context, GetUserListCallback callback);
    void getRandomItineraries(Context context, GetRandomItinerariesResultCallback callback);
}
