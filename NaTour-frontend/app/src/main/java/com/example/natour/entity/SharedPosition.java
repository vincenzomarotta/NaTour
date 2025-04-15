package com.example.natour.entity;

public class SharedPosition {
    public int id;
    public String user;
    public double latitude;
    public double longitude;

    public SharedPosition(){
    }

    public SharedPosition(int id, String user, double latitude, double longitude){
        this.id = id;
        this.user = user;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
