package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class App implements RequestHandler<Map<String, String>, Map<String,String>> {
    @Override
    public Map<String,String> handleRequest(final Map<String, String> input, final Context context) {
        Connection mySql;
        long itineraryId;
        String user;
        Itinerary itinerary;
        long favoriteListId = 0;
        long toVisitListId = 0;

        Map<String, String> response = new HashMap<>();

        if ((!input.containsKey("COMMAND")) || (!input.containsKey("ITINERARY_ID"))
                || (!input.containsKey("REQUEST_BY"))) {
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }
        if (!input.get("COMMAND").equals("DOWNLOAD_ITINERARY")) {
            System.out.println("COMMAND_NOT_VALID");
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
            if (!resultSet.next()) {
                mySql.close();
                response.put("RESPONSE", "USER_NOT_EXIST");
                return response;
            }
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Recupero dati base itinerario
        try {
            String query = "SELECT * FROM Itinerary WHERE id = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, itineraryId);

            ResultSet resultSet = preparedStatement.executeQuery();
            itinerary = new Itinerary();
            itinerary.wayPointsList = new LinkedList<>();
            if (resultSet.next()) {
                itinerary.id = resultSet.getLong("id");
                itinerary.ownerEmail = resultSet.getString("email");
                itinerary.title = resultSet.getString("title");
                itinerary.description = resultSet.getString("description");
                itinerary.state = resultSet.getString("state");
                itinerary.region = resultSet.getString("region");
                itinerary.city = resultSet.getString("city");
                itinerary.length = resultSet.getDouble("length");
                itinerary.duration = resultSet.getLong("duration");
                itinerary.difficulty = resultSet.getInt("difficulty");
                itinerary.accessibility = resultSet.getBoolean("access_dis");
                itinerary.isPrivate = resultSet.getBoolean("private");
            } else {
                mySql.close();
                response.put("RESPONSE", "ITINERARY_NOT_EXIST");
                return response;
            }
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Recupero lista waypoints
        try {
            String query = "SELECT latitude, longitude FROM Waypoints WHERE id = ? ORDER BY seq_number ASC";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, itineraryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            while (resultSet.next()) {
                LatLng latLng = new LatLng();
                latLng.latitude = resultSet.getDouble("latitude");
                latLng.longitude = resultSet.getDouble("longitude");
                itinerary.wayPointsList.add(latLng);
            }
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Recupero ultima modifica amministratore
        try {
            String query = "SELECT date, email FROM Edit WHERE id = ? order by date desc limit 1";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, itineraryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                itinerary.lastModificationDate = new Date(resultSet.getTimestamp("date").getTime());
                itinerary.lastModificationUser = resultSet.getString("email");
            }
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Recupero id lista preferiti
        try {
            String query = "SELECT idList FROM List WHERE email = ? AND type = \"FAVORITE\"";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                favoriteListId = resultSet.getLong("idList");
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Verifica appartenenza itinerario a lista preferiti
        try {
            String query = "SELECT * FROM ItineraryList WHERE idList = ? AND id = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, favoriteListId);
            preparedStatement.setLong(2, itineraryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                itinerary.isFavourite = true;
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Recupero id lista da visitare
        try {
            String query = "SELECT idList FROM List WHERE email = ? AND type = \"TO_VISIT\"";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                toVisitListId = resultSet.getLong("idList");
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Verifica appartenenza itinerario a lista da visitare
        try {
            String query = "SELECT * FROM ItineraryList WHERE idList = ? AND id = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, toVisitListId);
            preparedStatement.setLong(2, itineraryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                itinerary.isToVisit = true;
        } catch (SQLException e) {
            closeDbConnection(mySql);
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        // Chiusura connessione al db
        closeDbConnection(mySql);

        // Preparazione e invio json
        //ObjectMapper objectMapper = new ObjectMapper();
        
        //try {
            //Gson gson = new Gson();
            Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
            //String json = objectMapper.writeValueAsString(itinerary);
            String json = gson.toJson(itinerary);
            response.put("RESPONSE", "DOWNLOAD_ITINERARY_OK");
            response.put("JSON", json);
            return response;
        //}
        /*catch (JsonProcessingException e) {
            response.put("RESPONSE", "JSON_PROCESSING_ERROR");
            return response;
        }*/
    }

    private static void closeDbConnection(Connection mySql) {
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
