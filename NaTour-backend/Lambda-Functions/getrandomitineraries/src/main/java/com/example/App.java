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


public class App implements RequestHandler<Map<String, String>, Map<String, String>> {
    private static Connection mySql;

    @Override
    public Map<String, String> handleRequest(final Map<String, String> input, final Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> response = new HashMap<>();

        ArrayList<Itinerary> randomItineraries = new ArrayList<>();

        if(!input.containsKey("COMMAND") || !input.get("COMMAND").equals("GET_RANDOM_ITINERARIES")){
            response.put("RESPONSE", "COMMAND_NOT_VALID");
        }

        // DB Connection
        try {
            mySql = DriverManager.getConnection("jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
            "admin",
            "natour123");
        } catch (SQLException e) {
            response.put("RESPONSE", "DB_CONN_FAIL");
            return response;
        }

        try{
            String query = "SELECT id, title, description FROM Itinerary WHERE private = 'false' ORDER BY RAND() LIMIT 5;";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next()){
                Itinerary itinerary = new Itinerary();
                itinerary.id = resultSet.getLong("id");
                itinerary.title = resultSet.getString("title");
                itinerary.description = resultSet.getString("description");
                
                randomItineraries.add(itinerary);
            }

            preparedStatement.close();
            resultSet.close();
            closeDbConnection();

        }catch(SQLException e){
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        if(randomItineraries.size() == 0){
            response.put("RESPONSE", "NO_ITINERARIES_FOUND");
            return response;
        } 

        try {
            String json = objectMapper.writeValueAsString(randomItineraries);
            response.put("RANDOM_ITINERARIES", json);
            response.put("RESPONSE", "RETURN_RANDOM_OK");
            return response;
        } catch (JsonProcessingException e) {
            response.put("REPONSE", "JSON_PROCESSING_EXCEPTION");
            return response;
        }

    }

    private static void closeDbConnection(){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }

}
