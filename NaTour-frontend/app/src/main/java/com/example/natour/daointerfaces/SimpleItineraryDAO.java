package com.example.natour.daointerfaces;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.natour.entity.SimpleItinerary;

import java.util.ArrayList;
import java.util.List;

@Dao
public interface SimpleItineraryDAO {

    @Query("SELECT * FROM simple_itinerary WHERE list_type ='last_searched' ")
    List<SimpleItinerary> getLastSearched();

    @Query("SELECT * FROM simple_itinerary WHERE list_type ='favorites' ")
    List<SimpleItinerary> getFavourites();

    @Query("SELECT * FROM simple_itinerary WHERE list_type ='to_visit' ")
    List<SimpleItinerary> getToVisit();

    @Query("SELECT * FROM simple_itinerary WHERE list_type ='my_itinerary' ")
    List<SimpleItinerary> getMyItinerary();

    @Query("SELECT * FROM simple_itinerary WHERE id = :id AND list_type = :type")
    SimpleItinerary getItineraryByIdAndType(int id, String type);

    @Query("DELETE FROM simple_itinerary WHERE id = :id AND list_type = 'last_searched'")
    void takeOffFromLastSearched(int id);

    @Query("DELETE FROM simple_itinerary WHERE id = :id AND list_type = 'favorites'")
    void takeOffFromFavourite(int id);

    @Query("DELETE FROM simple_itinerary WHERE id = :id AND list_type = 'to_visit'")
    void takeOffFromToVisit(int id);

    @Query("DELETE FROM simple_itinerary WHERE list_type = 'last_searched'")
    void clearLastSearched();

    @Query("DELETE FROM simple_itinerary WHERE list_type = 'to_visit'")
    void clearToVisit();

    @Query("DELETE FROM simple_itinerary WHERE list_type = 'favorites'")
    void clearFavorites();

    @Query("DELETE FROM simple_itinerary WHERE list_type = 'my_itinerary'")
    void clearMyItinerary();

    @Query("DELETE FROM simple_itinerary WHERE id = :id")
    void takeOffFromItinerary(int id);

    @Query("SELECT * FROM simple_itinerary WHERE id = :id")
    List<SimpleItinerary> getItinerariesById(int id);

    @Insert
    void insertSimpleItinerary(SimpleItinerary ...simpleItineraries);

}