package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class App implements RequestHandler<Map<String,String>, Map<String,String>> {
    @Override
    public Map<String,String> handleRequest(final Map<String,String> input, final Context context) {
        Connection mySql;

        long itineraryId;
        String user;
        List<SharedPosition> sharedPositionList;

        Map<String,String> response = new HashMap<>();

        if ((!input.containsKey("COMMAND")) || (!input.containsKey("ITINERARY_ID"))
                || (!input.containsKey("REQUEST_BY"))) {
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }
        if (!input.get("COMMAND").equals("GET_SHARED_POSITION")) {
            response.put("RESPONSE", "COMMAND_NOT_VALID");
            return response;
        }
        itineraryId = Long.parseLong(input.get("ITINERARY_ID"));
        user = input.get("REQUEST_BY");

        // Connessione al database
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
            if(!resultSet.next()){
                mySql.close();
                response.put("RESPONSE", "USER_NOT_EXIST");
                return response;
            }
        } catch(SQLException e){
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Recupero posizioni utenti
        sharedPositionList = new ArrayList<>();
        try {
            String query = "SELECT email, latitude, longitude FROM SharePosition WHERE itineraryId = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, itineraryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while(resultSet.next()){
                SharedPosition sharedPosition = new SharedPosition();
                sharedPosition.id = itineraryId;
                sharedPosition.user = resultSet.getString("email");
                sharedPosition.latitude = resultSet.getDouble("latitude");
                sharedPosition.longitude = resultSet.getDouble("longitude");
                sharedPositionList.add(sharedPosition);
            }
        } catch(SQLException e){
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Chiusura connessione al database
        closeDbConnection(mySql);

        //Preparazione e invio json
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(sharedPositionList);
            response.put("RESPONSE", "GET_SHARED_POSITION_OK");
            response.put("JSON", json);
            return response;
        } catch (JsonProcessingException e) {
            response.put("RESPONSE", "JSON_PROCESSING_ERROR");
            return response;
        }
    }

    private static void closeDbConnection(Connection mySql){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
