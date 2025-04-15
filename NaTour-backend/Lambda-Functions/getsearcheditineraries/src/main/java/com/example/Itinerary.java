package com.example;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Locale;

public class Itinerary {
    public long id;
    public String ownerEmail = null;
    public String title = null;
    public String description = null;
    public String state = null;
    public String region = null;
    public String city = null;
    public double length = 0;
    public long duration = 0;
    public int difficulty = 0;
    public boolean accessibility = false;
    public boolean isPrivate = true;
    public LinkedList<LatLng> wayPointsList = null;
    public String lastModificationUser = null;
    public Date lastModificationDate = null;
    public boolean isFavourite = false;
    public boolean isToVisit = false;

    @Override
    public String toString() {
        StringBuilder output = new StringBuilder();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());

        output.append("Itinerary { \n\r");
        output.append(" Owner: ").append(ownerEmail).append("\n\r");
        output.append(" Title: ").append(title).append("\n\r");
        output.append(" State: ").append(state).append("\n\r");
        output.append(" Region: ").append(region).append("\n\r");
        output.append(" City: ").append(city).append("\n\r");
        output.append(" Length: ").append(length).append("\n\r");
        output.append(" Duration: ").append(duration).append("\n\r");
        output.append(" Difficulty: ").append(difficulty).append("\n\r");
        output.append(" Accessibility: ").append(accessibility).append("\n\r");
        output.append(" Is private: ").append(isPrivate).append("\n\r");
        output.append(" Description: ").append(description).append("\n\r");
        output.append(" Is favourite: ").append(isFavourite).append("\n\r");
        output.append(" Is to visit: ").append(isToVisit).append("\n\r");
        output.append(" Last modification user: ").append(lastModificationUser).append("\n\r");
        if (lastModificationDate != null)
            output.append(" Last modification date: ").append(dateFormat.format(lastModificationDate)).append("\n\r");
        if (wayPointsList != null) {
            output.append(" Waypoints list: \n\r");
            for (int i = 0; i < wayPointsList.size(); i++) {
                double latitude = wayPointsList.get(i).latitude;
                double longitude = wayPointsList.get(i).longitude;
                output.append(" Point[")
                        .append(i)
                        .append("]: ")
                        .append(latitude)
                        .append(" - ")
                        .append(longitude)
                        .append("\n\r");
            }
        }
        output.append("---------- END ----------\n\r");

        return output.toString();
    }
}
