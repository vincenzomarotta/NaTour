package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class App implements RequestHandler<Itinerary, Map<String,String>> {
    @Override
    public Map<String,String> handleRequest(final Itinerary input, final Context context) {
        Connection mySql;
        Itinerary itinerary;

        itinerary = input;
        Map<String,String> outputMap = new HashMap<>();

        // Connessione al database
        try {
            mySql = DriverManager.getConnection("jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
            "admin",
            "natour123");
        } catch (SQLException e) {
            outputMap.put("RESPONSE", "DB_CONN_FAIL");
            return outputMap;
        }

        // Inserimento dati base dell'itineario
        try {
            String query = "INSERT INTO Itinerary(id,title,description,difficulty,access_dis,private,state,region,city,length,duration,email)"
                +
                "VALUES (NULL,?,?,?,?,?,?,?,?,?,?,?);";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, itinerary.title);
            if ((itinerary.description == null) || (itinerary.description.length() == 0))
                preparedStatement.setNull(2, java.sql.Types.NULL);
            else
                preparedStatement.setString(2, itinerary.description);
            preparedStatement.setInt(3, itinerary.difficulty);
            preparedStatement.setBoolean(4, itinerary.accessibility);
            preparedStatement.setBoolean(5, itinerary.isPrivate);
            preparedStatement.setString(6, itinerary.state);
            preparedStatement.setString(7, itinerary.region);
            preparedStatement.setString(8, itinerary.city);
            preparedStatement.setDouble(9, itinerary.length);
            preparedStatement.setLong(10, itinerary.duration);
            preparedStatement.setString(11, itinerary.ownerEmail);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            closeDbConnection(mySql);
            outputMap.put("RESPONSE", "SQL_EXCEPTION");
            return outputMap;
        }

        // Recupero id del nuovo itinerario
        long newId = 0;
        try {
            String query = "SELECT i.id FROM Itinerary AS i WHERE i.email = ? ORDER BY i.id DESC LIMIT 1;";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, itinerary.ownerEmail);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                newId = resultSet.getLong("id");
            } else {
                throw new SQLException();
            }
        } catch (SQLException e) {
            closeDbConnection(mySql);
            outputMap.put("RESPONSE", "SQL_EXCEPTION");
            return outputMap;
        }

        // Inserimento dei waypoint
        // Cancellazione dei dati precedentemente caricati (ai punti sopra) in caso di errore.
        try {
            int argNumber = 1;
            String query = "INSERT INTO Waypoints(seq_number,latitude,longitude,id) VALUES";
            query = query.concat("(?,?,?,?)");
            for (int i = 1; i < itinerary.wayPointsList.size(); i++) {
                query = query.concat(",(?,?,?,?)");
            }
            query = query.concat(";");

            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            for (int i = 0; i < itinerary.wayPointsList.size(); i++) {
                preparedStatement.setInt(argNumber, i);
                argNumber++;
                preparedStatement.setDouble(argNumber, itinerary.wayPointsList.get(i).latitude);
                argNumber++;
                preparedStatement.setDouble(argNumber, itinerary.wayPointsList.get(i).longitude);
                argNumber++;
                preparedStatement.setLong(argNumber, newId);
                argNumber++;
            }
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            try {
                String deleteQuery = "DELETE FROM Waypoints WHERE id = " + Long.toString(newId) + ";";
                Statement statement = mySql.createStatement();
                statement.execute(deleteQuery);
                deleteQuery = "DELETE FROM Itinerary WHERE id = " + Long.toString(newId) + ";";
                statement = mySql.createStatement();
                statement.execute(deleteQuery);
                closeDbConnection(mySql);
            } catch (SQLException e1) {
                closeDbConnection(mySql);
                outputMap.put("RESPONSE", "SQL_EXCEPTION");
            return outputMap;
            }
            outputMap.put("RESPONSE", "SQL_EXCEPTION");
            return outputMap;
        }

        // Chiusura della connessione al database
        closeDbConnection(mySql);

        // Invio responso
        outputMap.put("RESPONSE", "UPLOAD_ITINERARY_OK");
        outputMap.put("NEW_ID", Long.toString(newId));
        return outputMap;
    }

    private static void closeDbConnection(Connection mySql){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
