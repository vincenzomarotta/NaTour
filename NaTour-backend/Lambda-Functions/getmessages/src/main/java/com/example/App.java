package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


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
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, String> response = new HashMap<>();
        ArrayList<Message> messageList = new ArrayList<>();

        // Checking if the input is correct
        if(!input.containsKey("COMMAND") || !input.containsKey("SENDER")  || !input.containsKey("RECEIVER")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!(input.get("COMMAND").equals("GET_MESSAGES"))){
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
        String receiver =  input.get("RECEIVER");
        String sender = input.get("SENDER");
        String query = "SELECT M.emailDest, M.emailSend, M.message, M.date, M.isSeen"
                        +" FROM Message M"
                        +" WHERE (M.emailDest = ?  AND M.emailSend = ?) OR (M.emailDest = ? AND M.emailSend = ?) ORDER BY (M.date)";

        // Getting messages
        try {
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, receiver);
            preparedStatement.setString(2, sender);
            preparedStatement.setString(3, sender);
            preparedStatement.setString(4, receiver);
           
            ResultSet resultSet = preparedStatement.executeQuery();
            int i=0;
            while(resultSet.next()){
                Message message = new Message();
                message.sender = (resultSet.getString("emailSend"));
                message.receiver = (resultSet.getString("emailDest"));
                message.body =(resultSet.getString("message"));
                message.date = (resultSet.getTimestamp("date"));
                message.seen = (resultSet.getBoolean("isSeen"));
                messageList.add(message);
            }
            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        if(messageList.size()==0){
            response.put("RESPONSE","NO_MESSAGE_FOUND");
            return response;
        }
        // Returning messages
        String updateQuery = "UPDATE Message M SET M.isSeen = TRUE WHERE M.emailSend = ? AND M.emailDest = ? AND M.isSeen = FALSE;";
        try{
            PreparedStatement preparedStatement = mySql.prepareStatement(updateQuery);
            preparedStatement.setString(1, receiver);
            preparedStatement.setString(2, sender);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            closeDbConnection();
        }catch(SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION_UPDATE"+e.getMessage());
            return response;
        }

        try{
            String json = objectMapper.writeValueAsString(messageList);
            response.put("MESSAGES", json);
            response.put("RESPONSE", "RETURN_MESSAGES_OK");
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
