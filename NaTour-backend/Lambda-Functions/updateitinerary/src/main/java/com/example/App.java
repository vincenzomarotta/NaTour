package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class App implements RequestHandler<Itinerary, String> {
    @Override
    public String handleRequest(final Itinerary input, final Context context) {
        Connection mySql;
        Itinerary itinerary;

        itinerary = input;

        // Connessione al database
        try {
            mySql = DriverManager.getConnection("jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
            "admin",
            "natour123");
        } catch (SQLException e) {
            return "DB_CONN_FAIL";
        }

        // Cancellazione waypoint precedenti
        try {
            String query = "DELETE FROM Waypoints WHERE id = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setLong(1, itinerary.id);
            preparedStatement.executeUpdate();
        } catch(SQLException e) {
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Aggiornamento dati itinerario
        try {
            String query = "UPDATE Itinerary SET " +
            "title = ?, description = ?, difficulty = ?, access_dis = ?, private = ?," +
            "state = ?, region = ?, city = ?, length = ?, duration = ? " +
            "WHERE id = ?";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, itinerary.title);
            if((itinerary.description == null) || (itinerary.description.length() == 0))
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
            preparedStatement.setLong(11, itinerary.id);
            preparedStatement.executeUpdate();
        } catch(SQLException e){
            return "SQL_EXCEPTION";
        }

        // Inserimento nuovi waypoint
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
                preparedStatement.setLong(argNumber, itinerary.id);
                argNumber++;
            }
            preparedStatement.executeUpdate();
        } catch(SQLException e) {
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Inserimento dati della modifica
        try {
            String query = "INSERT INTO Edit(date,email,id) VALUES (?,?,?)";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setTimestamp(1, new Timestamp(itinerary.lastModificationDate.getTime()));
            preparedStatement.setString(2, itinerary.lastModificationUser);
            preparedStatement.setLong(3, itinerary.id);
            preparedStatement.executeUpdate();
        } catch(SQLException e) {
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Chiusura della connessione al database
        closeDbConnection(mySql);

        // Invio responso
        return "UPDATE_ITINERARY_OK";
    }

    private static void closeDbConnection(Connection mySql){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
