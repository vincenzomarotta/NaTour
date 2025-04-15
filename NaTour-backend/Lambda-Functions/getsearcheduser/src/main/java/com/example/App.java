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
        ArrayList<User> userList = new ArrayList<>();
        User user;

        // Checking if the input is correct
        if(!input.containsKey("COMMAND") || !input.containsKey("SEARCH")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!(input.get("COMMAND").equals("GET_SEARCHED_USERS"))){
            response.put("RESPONSE", "COMMAND_NOT_VALID");
            return response;
        }

        // Checking db conenction
        try {
            mySql = DriverManager.getConnection(
                    "jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
                    "admin",
                    "natour123");
        } catch (SQLException e) {
            response.put("RESPONSE", "DB_CONN_FAIL");
            return response;
        }

        String query = "SELECT * FROM User U WHERE U.email LIKE ? OR U.name LIKE ? OR U.surname LIKE ?;";
        String tmpUser = "%"+input.get("SEARCH")+"%";

        // Getting users
        try {

            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, tmpUser);
            preparedStatement.setString(2, tmpUser);
            preparedStatement.setString(3, tmpUser);

            ResultSet resultSet = preparedStatement.executeQuery();

            while(resultSet.next()){
                user = new User();
                user.setEmail(resultSet.getString("email"));
                user.setName(resultSet.getString("name"));
                user.setSurname(resultSet.getString("surname"));
                user.setAdmin(resultSet.getBoolean("isAdmin"));


                userList.add(user);
            }

            preparedStatement.close();
            resultSet.close();
            closeDbConnection();

        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION" + e.getMessage());
            return response;
        }

        if(userList.size() == 0){
            response.put("RESPONSE", "NO_USER_FOUND");
            return response;
        }
        // Returning users
        try{
            String json = objectMapper.writeValueAsString(userList);
            response.put("USERS", json);
            response.put("RESPONSE", "RETURN_USERS_OK");
            return response;

        } catch(JsonProcessingException e){
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
