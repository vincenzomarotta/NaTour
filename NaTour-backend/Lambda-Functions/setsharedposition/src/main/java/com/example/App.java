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
        SharedPosition sharedPosition = new SharedPosition();
        Connection mySql;
        boolean oldPositionExist = false;

        // Controllo comandi
        if ((!input.containsKey("COMMAND")) || (!input.containsKey("ITINERARY_ID"))
                || (!input.containsKey("REQUEST_BY"))) {
            return "REQUEST_NOT_VALID";
        }
        if (!input.get("COMMAND").equals("SET_SHARED_POSITION")) {
            return "COMMAND_NOT_VALID";
        }
        sharedPosition.id = Long.parseLong(input.get("ITINERARY_ID"));
        sharedPosition.user = input.get("REQUEST_BY");
        sharedPosition.latitude = Double.parseDouble(input.get("LATITUDE"));
        sharedPosition.longitude = Double.parseDouble(input.get("LONGITUDE"));

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
            preparedStatement.setString(1, sharedPosition.user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(!resultSet.next()){
                mySql.close();
                return "USER_NOT_EXIST";
            }
        } catch(SQLException e){
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Verifica esistenza vecchia posizione
        try {
            String query = "SELECT * FROM SharePosition WHERE itineraryId = ? AND email = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, sharedPosition.id);
            preparedStatement.setString(2, sharedPosition.user);
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next())
                oldPositionExist = true;
        } catch(SQLException e){
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Aggiornamento/Inserimento dati
        if(oldPositionExist){
            try {
                String query = "UPDATE SharePosition SET latitude = ?, longitude = ? "+
                    "WHERE email = ? AND itineraryId = ?";
                PreparedStatement preparedStatement = mySql.prepareStatement(query);
                preparedStatement.setDouble(1, sharedPosition.latitude);
                preparedStatement.setDouble(2, sharedPosition.longitude);
                preparedStatement.setString(3, sharedPosition.user);
                preparedStatement.setLong(4, sharedPosition.id);
                preparedStatement.executeUpdate();
            } catch(SQLException e){
                closeDbConnection(mySql);
                return "SQL_EXCEPTION";
            }
        } else{
            try {
                String query = "INSERT INTO SharePosition(email, itineraryId, latitude, longitude) VALUES (?,?,?,?)";
                PreparedStatement preparedStatement = mySql.prepareStatement(query);
                preparedStatement.setString(1, sharedPosition.user);
                preparedStatement.setLong(2, sharedPosition.id);
                preparedStatement.setDouble(3, sharedPosition.latitude);
                preparedStatement.setDouble(4, sharedPosition.longitude);
                preparedStatement.executeUpdate();
            } catch(SQLException e){
                closeDbConnection(mySql);
                return "SQL_EXCEPTION";
            }
        }

        // Chiusura connessione al database
        closeDbConnection(mySql);

        // Invio responso
        return "SET_SHARED_POSITION_OK";
    }

    private static void closeDbConnection(Connection mySql){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
