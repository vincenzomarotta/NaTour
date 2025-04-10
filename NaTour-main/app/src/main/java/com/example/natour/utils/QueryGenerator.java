package com.example.natour.utils;

import java.util.HashMap;
import java.util.Map;

public class QueryGenerator {

    private String search;

    public QueryGenerator(){

    }

    public QueryGenerator(String search){
        this.search = search;
    }


    public String createFilterQuery(Map<String, String> filter){

        if(search == null)
            throw new NullPointerException();

        String query = "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%"+search+"%'";

        if(filter == null)
            return query+ ";";

        if(filter.containsKey("Place")) {
            if (!(filter.get("Place").isEmpty())) {
                query = query + " AND (I.State LIKE '%" + filter.get("Place") + "%' OR I.Region LIKE '%" + filter.get("Place") + "%' OR I.City LIKE '%" + filter.get("Place") + "%')";
            }
        }
        if(filter.containsKey("Difficulty")) {
            if ((Integer.valueOf(filter.get("Difficulty"))>0 && (Integer.valueOf(filter.get("Difficulty")) < 6))) {
                query = query + " AND I.Difficulty <= " + filter.get("Difficulty");
            } else
                throw new IllegalArgumentException();
        }
        if(filter.containsKey("Duration")) {
            if ((Integer.valueOf(filter.get("Duration")) > 0 && (Integer.valueOf(filter.get("Duration")) < 11))) {
                int durationInSeconds = Integer.valueOf(filter.get("Duration")) * 3600;
                query = query + " AND I.Duration <= " + durationInSeconds;
            } else
                throw new IllegalArgumentException();
        }
        if(filter.containsKey("DisabledAccess")) {
                query = query + " AND I.access_dis = " + filter.get("DisabledAccess");
        }

        query = query+";";


        return query;
    }

    public String createFilterQuery(String search, Map<String, String> filter){

        if(search == null)
            throw new NullPointerException();

        String query = "SELECT I.id, I.title, I.description FROM Itinerary I WHERE I.private = FALSE AND I.title LIKE '%"+search+"%'";

        if(filter == null)
            return query+ ";";

        if(filter.containsKey("Place")) {
            if (!(filter.get("Place").isEmpty())) {
                query = query + " AND (I.State LIKE '%" + filter.get("Place") + "%' OR I.Region LIKE '%" + filter.get("Place") + "%' OR I.City LIKE '%" + filter.get("Place") + "%')";
            }
        }
        if(filter.containsKey("Difficulty")) {
            if ((Integer.valueOf(filter.get("Difficulty"))>0 && (Integer.valueOf(filter.get("Difficulty")) < 6))) {
                query = query + " AND I.Difficulty <= " + filter.get("Difficulty");
            } else
                throw new IllegalArgumentException();
        }
        if(filter.containsKey("Duration")) {
            if ((Integer.valueOf(filter.get("Duration")) > 0 && (Integer.valueOf(filter.get("Duration")) < 11))) {
                int durationInSeconds = Integer.valueOf(filter.get("Duration")) * 3600;
                query = query + " AND I.Duration <= " + durationInSeconds;
            } else
                throw new IllegalArgumentException();
        }
        if(filter.containsKey("DisabledAccess")) {
            query = query + " AND I.access_dis = " + filter.get("DisabledAccess");
        }

        query = query+";";


        return query;
    }


}
