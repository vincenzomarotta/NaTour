package com.example;

public class SharedPosition {
    public long id;
    public String user;
    public double latitude;
    public double longitude;

    @Override
    public String toString() {
        return Long.toString(id) + " - " + user + " - " + Double.toString(latitude) + " - " + Double.toString(longitude);
    }
}
