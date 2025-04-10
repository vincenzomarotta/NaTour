package com.example.natour.utils;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.natour.daointerfaces.SimpleItineraryDAO;
import com.example.natour.entity.SimpleItinerary;

@Database(entities = {SimpleItinerary.class}, version = 7)
public abstract class MyDatabase extends RoomDatabase {

    private static MyDatabase instance;
    private static Context context;

    public abstract SimpleItineraryDAO simpleItineraryDAO();

    /**
     * Creates an instance of RoomDatabase.
     * @param context context of the application.
     * @return MyDatabase Room instance.
     */
    public static MyDatabase getInstance(Context context){
        if (instance == null){
            instance = Room.databaseBuilder(context, MyDatabase.class, "Itineraries")
                    .allowMainThreadQueries()
                    .fallbackToDestructiveMigrationFrom(6)
                    .fallbackToDestructiveMigrationFrom(5)
                    .fallbackToDestructiveMigrationFrom(4)
                    .fallbackToDestructiveMigrationFrom(3)
                    .fallbackToDestructiveMigrationFrom(2)
                    .fallbackToDestructiveMigrationFrom(1)
                    .build();
        }
        return instance;
    }

    /**
     * Destroys the instance.
     */
    public static void destroyInstance(){
        instance = null;
    }
}
