package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;

public class App implements RequestHandler<Map<String, String>, Map<String, String>> {
    private static Connection mySql;

    @Override
    public Map<String, String> handleRequest(final Map<String, String> input, final Context context) {
        Map<String, String> response = new HashMap<>();

        if(!input.containsKey("COMMAND") || !input.containsKey("EMAIL")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!(input.get("COMMAND").equals("UPDATE_USER_ADMIN"))){
            response.put("RESPONSE", "COMMAND_NOT_VALID");
            return response;
        }

        try {
            mySql = DriverManager.getConnection(
                    "jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
                    "admin",
                    "natour123");
        } catch (SQLException e) {
            response.put("RESPONSE", "DB_CONN_FAIL");
            return response;
        }

        // Checking if user exists
        String queryUserExists = "SELECT * FROM User WHERE email = ?;";
        try {
            PreparedStatement preparedStatement = mySql.prepareStatement(queryUserExists);
            preparedStatement.setString(1, input.get("EMAIL"));

            ResultSet rs = preparedStatement.executeQuery();

            if (!rs.next()) {
                closeDbConnection();
                response.put("RESPONSE", "USER_NOT_FOUND");
                return response;
            }

        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }


        // Update user setting admin true
        String queryUpdateUser = "UPDATE User SET isAdmin = true WHERE email = ?;";

        try {
            PreparedStatement preparedStatement = mySql.prepareStatement(queryUpdateUser);
            preparedStatement.setString(1, input.get("EMAIL"));
            preparedStatement.executeUpdate();
            mySql.close();
            response.put("RESPONSE", "UPDATE_USER_ADMIN_OK");
            return response;
        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION");
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
