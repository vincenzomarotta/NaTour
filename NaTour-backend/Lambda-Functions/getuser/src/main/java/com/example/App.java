package com.example;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
//import com.google.gson.Gson;
//import com.google.gson.JsonSyntaxException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class App implements RequestHandler<Map<String,String>, Map<String,String>> {
    private static Connection mySql;
    User newUser;

    @Override
    public Map<String,String> handleRequest(final Map<String,String> input, final Context context) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> response = new HashMap<>();

        if(!input.containsKey("COMMAND") || !input.containsKey("USER")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!(input.get("COMMAND").equals("SET_USER"))){
            response.put("RESPONSE", "COMMAND_NOT_VALID");
            return response;
        }

        // Getting user 
        User inputUser;
        try {
            inputUser = objectMapper.readValue(input.get("USER"), User.class);
        } catch (JsonMappingException e1) {
            response.put("RESPONSE", "JSON_MAPPING_EXCEPTION");
            return response;
        } catch (JsonProcessingException e1) {
            response.put("RESPONSE", "JSON_PROCESSING_EXCEPTION");
            return response;
        }
                
        // DB connection 
        try {
            mySql = DriverManager.getConnection(
                    "jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
                    "admin",
                    "natour123");
        } catch (SQLException e) {
            response.put("RESPONSE", "DB_CONN_FAIL");
            return response;
        }


        String query = "SELECT * FROM User WHERE email = ?;";
        try {
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, inputUser.getEmail());

            ResultSet rs = preparedStatement.executeQuery();

            if (!rs.next()) {
                closeDbConnection();
                String answer = addUser(inputUser);
                response.put("RESPONSE", answer);
                return response;
            }

            newUser = new User();
            newUser.setEmail(rs.getString("email"));
            newUser.setName(rs.getString("name"));
            newUser.setSurname(rs.getString("surname"));
            newUser.setAdmin(rs.getBoolean("isAdmin"));

            closeDbConnection();

        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION_GET_USER");
            return response;
        }
            
        try {
            String json = objectMapper.writeValueAsString(newUser);
            response.put("RETURN_USER", json);
            response.put("RESPONSE", "RETURN_GET_USER_OK");
            return response;
        } catch (JsonProcessingException e) {
            response.put("RESPONSE", "JSON_PROCESSING_EXCEPTION");
            return response;
        }

    }

    private String addUser(User user){
        try {
            mySql = DriverManager.getConnection("jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
            "admin",
            "natour123");
        } catch (SQLException e) {
            return "DB_CONN_FAIL";
        }

        // Inseriemento dati utente
        try {
            String query = "INSERT INTO User(email, name, surname, isAdmin) VALUES (?,?,?,?)";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, user.getEmail());
            preparedStatement.setString(2, user.getName());
            preparedStatement.setString(3, user.getSurname());
            preparedStatement.setBoolean(4, user.isAdmin());
            preparedStatement.executeUpdate();
        } catch(SQLException e){
            closeDbConnection();
            return "SQL_EXCEPTION_ADD_USER";
        }

        // Chiusura della connessione al database
        closeDbConnection();

        //Invio responso
        return "UPLOAD_USER_OK";
    }

    private static void closeDbConnection(){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }

}
