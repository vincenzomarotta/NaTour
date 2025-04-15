package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

public class App implements RequestHandler<User, String> {
    @Override
    public String handleRequest(final User input, final Context context) {
        Connection mySql;

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
            preparedStatement.setString(1, input.getEmail());
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()){
                closeDbConnection(mySql);
                return "USER_ALREDY_EXIST";
            }
        } catch(SQLException e){
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Inseriemento dati utente
        try {
            String query = "INSERT INTO User(Name,Surname,Email,isAdmin) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, input.getName());
            preparedStatement.setString(2, input.getSurname());
            preparedStatement.setString(3, input.getEmail());
            preparedStatement.setBoolean(4, input.isAdmin());
            preparedStatement.executeUpdate();
        } catch(SQLException e){
            closeDbConnection(mySql);
            return "SQL_EXCEPTION";
        }

        // Chiusura della connessione al database
        closeDbConnection(mySql);

        //Invio responso
        return "UPLOAD_USER_OK";
    }

    private static void closeDbConnection(Connection mySql){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
