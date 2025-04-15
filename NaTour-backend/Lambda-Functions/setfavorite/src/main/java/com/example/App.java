package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class App implements RequestHandler<Map<String,String>, String> {
    @Override
    public String handleRequest(final Map<String,String> input, final Context context) {
        Connection mySql;

        long itineraryId = 0;
        String user;
        boolean isFavorite;
        long listId = 0;

        boolean existInList = false;

        // Controllo comandi
        if ((!input.containsKey("COMMAND")) || (!input.containsKey("ITINERARY_ID"))
                || (!input.containsKey("REQUEST_BY")) ||
                (!input.containsKey("FAVORITE"))) {
            return "REQUEST_NOT_VALID";
        }
        if (!input.get("COMMAND").equals("SET_LIST")) {
            return "COMMAND_NOT_VALID";
        }
        user = input.get("REQUEST_BY");
        itineraryId = Long.parseLong(input.get("ITINERARY_ID"));
        isFavorite = Boolean.parseBoolean(input.get("FAVORITE"));

        // Connessione al database
        try {
            mySql = DriverManager.getConnection("jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
            "admin",
            "natour123");
        } catch (SQLException e) {
            return "DB_CONN_FAIL";
        }

        // Verifica esistenza utente
        try {
            String query = "SELECT * FROM User WHERE email = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (!resultSet.next()) {
                mySql.close();
                return "USER_NOT_EXIST";
            }
        } catch (SQLException e) {
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Recupero id lista preferiti
        try {
            String query = "SELECT idList FROM List WHERE email = ? AND type = \"FAVORITE\"";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                listId = resultSet.getLong("idList");
        } catch (SQLException e) {
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Verifica dell'esistenza dell'itinerario in lista
        try {
            String query = "SELECT * FROM ItineraryList WHERE idList = ? AND id = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, listId);
            preparedStatement.setLong(2, itineraryId);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                existInList = true;
        } catch (SQLException e) {
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Settaggio della lista
        if (isFavorite) {
            if (!existInList) {
                try {
                    String query = "INSERT INTO ItineraryList(idList,id) VALUES (?,?)";
                    PreparedStatement preparedStatement = mySql.prepareStatement(query);
                    preparedStatement.setLong(1, listId);
                    preparedStatement.setLong(2, itineraryId);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    closeDbConnection(mySql);
                    return "SQL_EXCEPTION";
                }
            }
        } else {
            if(existInList){
                try {
                    String query = "DELETE FROM ItineraryList WHERE idList = ? AND id = ?";
                    PreparedStatement preparedStatement = mySql.prepareStatement(query);
                    preparedStatement.setLong(1, listId);
                    preparedStatement.setLong(2, itineraryId);
                    preparedStatement.executeUpdate();
                } catch (SQLException e) {
                    closeDbConnection(mySql);
                    return "SQL_EXCEPTION";
                }
            }
        }

        // Chiusura connessione al db
        closeDbConnection(mySql);

        // Invio responso
        return "FAVORITE=" + input.get("FAVORITE");
    }

    private static void closeDbConnection(Connection mySql){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
