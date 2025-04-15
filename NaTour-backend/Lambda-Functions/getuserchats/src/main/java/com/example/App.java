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
import software.amazon.awssdk.services.lambda.LambdaClient;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<Map<String,String>, Map<String,String>> {
    private static Connection mySql;

    @Override
    public Map<String,String> handleRequest(final Map<String,String> input, final Context context) {
        ArrayList<SimpleMessage> chatList = new ArrayList<>();
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> response = new HashMap<>();

        // Checking if the input is correct
        if(!input.containsKey("COMMAND") || !input.containsKey("SENDER")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!(input.get("COMMAND").equals("GET_USER_CHAT"))){
            response.put("RESPONSE", "COMMAND_NOT_VALID");
            return response;
        }

        // Checking db connecction
        try {
            mySql = DriverManager.getConnection(
                    "jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
                    "admin",
                    "natour123");
        } catch (SQLException e) {
            response.put("RESPONSE", "DB_CONN_FAIL");
            return response;
        }
        String sender = input.get("SENDER");
        String query = "SELECT DISTINCT U.email, U.name, U.surname FROM Message M JOIN User U ON U.email = M.emailDest OR U.email = M.emailSend WHERE ((M.emailDest = ? OR M.emailSend = ?) AND U.email NOT IN(SELECT US.email FROM User US WHERE US.email=?));";

        // Getting messages
        try {
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, sender);
            preparedStatement.setString(2, sender);
            preparedStatement.setString(3, sender);
           
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next()){
                SimpleMessage chat = new SimpleMessage();
                chat.email = (resultSet.getString("email"));
                chat.name = (resultSet.getString("name"));
                chat.surname =(resultSet.getString("surname"));
                chatList.add(chat);
            }
            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION "+e.getMessage());
            return response;
        }

        if(chatList.size()==0){
            response.put("RESPONSE","NO_CHAT_FOUND");
            return response;
        }

        String queryForChat = "SELECT M.message"
                            +" FROM Message M" 
                            +" WHERE (M.emailDest = ? AND M.emailSend = ?) OR (M.emailDest = ? AND M.emailSend = ?)"
                             +" ORDER BY(M.date) DESC"
                             +" LIMIT 1;";

        for (SimpleMessage chat : chatList) {
            try {
                PreparedStatement preparedStatement = mySql.prepareStatement(queryForChat);
                preparedStatement.setString(1, sender);
                preparedStatement.setString(2, chat.email);
                preparedStatement.setString(3, chat.email);
                preparedStatement.setString(4, sender);
               
                ResultSet resultSet = preparedStatement.executeQuery();
                
                while(resultSet.next()){
                    chat.message = (resultSet.getString("message"));
                }
                preparedStatement.close();
                resultSet.close();

            } catch (SQLException e) {
                closeDbConnection();
                response.put("RESPONSE", "SQL_EXCEPTION FOR SINGLE CHAT ELEMENT "+e.getMessage());
                return response;
            }
        }
            closeDbConnection();
        // Returning messages
        try{
            String json = objectMapper.writeValueAsString(chatList);
            response.put("CHATS", json);
            response.put("RESPONSE", "RETURN_CHAT_OK");
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

