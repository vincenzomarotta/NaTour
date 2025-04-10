package com.example.natour.utils;

import android.location.Location;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.joda.time.Duration;

import java.util.List;

public class GeoUtils {
    public final static int WALK = 0;
    public final static int BIKE = 1;

    /**
     * Return the distance in meters of the list of way points.
     * @param wayPointsList list of LatLng coordinates.
     * @return distance in meters.
     */
    public double getRouteLength(@NonNull List<LatLng> wayPointsList){
        if(wayPointsList.size() < 2)
            return 0d;

        Location locA = new Location("A");
        LatLng coordA;
        Location locB = new Location("B");
        LatLng coordB;
        double routeLength = 0;
        double legDistance;
        for(int i = 1; i < wayPointsList.size(); i++){
            coordA = wayPointsList.get(i);
            coordB = wayPointsList.get(i-1);
            locA.setLatitude(coordA.latitude);
            locA.setLongitude(coordA.longitude);
            locB.setLatitude(coordB.latitude);
            locB.setLongitude(coordB.longitude);
            legDistance = locA.distanceTo(locB);
            routeLength += legDistance;
        }
        return routeLength;
    }

    /**
     * Return travel time in seconds.
     * The time is calculated based on the average speed.
     * You can choose the average speed of reference with the constants WALK and BIKE.
     * @param wayPointsList list of LatLng coordinates.
     * @param travelMode costant WALK or BIKE.
     * @return travel time in seconds.
     */
    public long getTravelTime(@NonNull List<LatLng> wayPointsList, int travelMode){
        double routeLenth = getRouteLength(wayPointsList);

        switch(travelMode) {
            case WALK:
                return (long) (routeLenth / 1.66667);
            case BIKE:
                return (long) (routeLenth / 6.11111);
            default:
                return 0;
        }
    }

    /**
     * Return travel time into Duration object.
     * The time is calculated based on the average speed.
     * You can choose the average speed of reference with the constants WALK and BIKE.
     * @param wayPointsList list of LatLng coordinates.
     * @param travelMode costant WALK or BIKE.
     * @return travel time into Duration object.
     */
    public Duration getTravelTimeDuration(List<LatLng> wayPointsList, int travelMode) {

        if(wayPointsList == null)
            throw new NullPointerException();

        double routeLength = getRouteLength(wayPointsList);
        long travelTime;
        Duration time;

        switch(travelMode) {
            case WALK:
                travelTime = (long) (routeLength / 1.66667);
                break;
            case BIKE:
                travelTime = (long) (routeLength / 6.11111);
                break;
            default:
                return null;
        }
        time = Duration.standardSeconds(travelTime);
        return time;
    }

    /**
     * Convert seconds into formatted string hh:mm:ss
     * @param seconds seconds to conver
     * @return formatted string
     */
    public String convertSecondToFormattedString(long seconds){
        long tempHours = seconds/3600;
        long tempMinutes = (seconds%3600)/60;
        long tempSeconds = (seconds%60);
        return String.format("%02d:%02d:%02d", tempHours, tempMinutes, tempSeconds);
    }
}
