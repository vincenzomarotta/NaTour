package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App implements RequestHandler<Map<String,String>, Map<String,String>> {
    private static Connection mySql;
    

    @Override
    public Map<String,String> handleRequest(final Map<String,String> input, final Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> response = new HashMap<>();

        String user;

        ArrayList<Itinerary> toVisitList = new ArrayList<>();
        ArrayList<Itinerary> favoriteList = new ArrayList<>();
        ArrayList<Itinerary> myItineraryList = new ArrayList<>();

        if(!input.containsKey("COMMAND") || !input.containsKey("REQUEST_BY")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!input.get("COMMAND").equals("GET_LISTS")){
            response.put("RESPONSE", "COMMAND_NOT_VALID");
            return response;
        }

        user = input.get("REQUEST_BY");

        // DB Connection
        try {
            mySql = DriverManager.getConnection("jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
            "admin",
            "natour123");
        } catch (SQLException e) {
            response.put("RESPONSE", "DB_CONN_FAIL");
            return response;
        }

         // Verifica esistenza utente
         try {
            String query = "SELECT * FROM User WHERE email = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                mySql.close();
                response.put("RESPONSE", "USER_NOT_EXIST");
                return response;
            }

        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Getting To Visit List
        try {
            String query = "SELECT I.id, I.title, I.description " +
            "FROM List L NATURAL JOIN ItineraryList IL NATURAL JOIN Itinerary I " +
            "WHERE L.email = ? AND L.type = 'TO_VISIT';";

            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                Itinerary itinerary = new Itinerary();
                itinerary.id = resultSet.getLong("id");
                itinerary.title = resultSet.getString("title");
                itinerary.description = resultSet.getString("description");

                toVisitList.add(itinerary);
            }

            if(toVisitList.size() != 0){
                String visitJson;
                try {
                    visitJson = objectMapper.writeValueAsString(toVisitList);
                    response.put("TO_VISIT", visitJson);
                } catch (JsonProcessingException e) {
                    closeDbConnection();
                    response.put("RESPONSE", "VISIT_JSON_PROCESSING_EXCEPTION" + e.getMessage());
                    return response;
                }
            } else{
                response.put("TO_VISIT", null);
            }
            
            resultSet.close();
            preparedStatement.close();
            
        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "VISIT_SQL_EXCEPTION");
            return response;
        }


        // Getting Favorite List
        try {
            String query = "SELECT I.id, I.title, I.description " +
            "FROM List L NATURAL JOIN ItineraryList IL NATURAL JOIN Itinerary I " +
            "WHERE L.email = ? AND L.type = 'FAVORITE';";

            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                Itinerary itinerary = new Itinerary();
                itinerary.id = resultSet.getLong("id");
                itinerary.title = resultSet.getString("title");
                itinerary.description = resultSet.getString("description");

                favoriteList.add(itinerary);
            }

            if(favoriteList.size() != 0){
                String favoriteJson;
                objectMapper = new ObjectMapper();
                try {
                    favoriteJson = objectMapper.writeValueAsString(favoriteList);
                    response.put("FAVORITE", favoriteJson);
                } catch (JsonProcessingException e) {
                    closeDbConnection();
                    response.put("RESPONSE", "FAVORITE_JSON_PROCESSING_EXCEPTION");
                    return response;
                }
            } else{
                response.put("FAVORITE", null);
            }

            resultSet.close();
            preparedStatement.close();

        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "FAVORITE_SQL_EXCEPTION" + e.getMessage());
            return response;
        }

        // Getting My Itinerary List
        try {
            String query = "SELECT I.id, I.title, I.description " +
            "FROM Itinerary I " +
            "WHERE I.email = ? " +
            "ORDER BY I.id DESC;";

            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                Itinerary itinerary = new Itinerary();
                itinerary.id = resultSet.getLong("id");
                itinerary.title = resultSet.getString("title");
                itinerary.description = resultSet.getString("description");

                myItineraryList.add(itinerary);
            }

            if(myItineraryList.size() != 0){
                String myItineraryJson;
                objectMapper = new ObjectMapper();
                try {
                    myItineraryJson = objectMapper.writeValueAsString(myItineraryList);
                    response.put("MY_ITINERARY", myItineraryJson);
                } catch (JsonProcessingException e) {
                    closeDbConnection();
                    response.put("RESPONSE", "MY_ITINERARY_JSON_PROCESSING_EXCEPTION");
                    return response;
                }
            }
            else{
                response.put("MY_ITINERARY", null);
            }

            resultSet.close();
            preparedStatement.close();
            
        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "MY_ITINERARY_SQL_EXCEPTION" + e.getMessage());
            return response;
        }

        response.put("RESPONSE", "GET_LIST_OK");
        return response;
    }

    private static void closeDbConnection(){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }

}
