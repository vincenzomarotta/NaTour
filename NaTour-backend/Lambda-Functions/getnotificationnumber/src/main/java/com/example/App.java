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
        int counter = 0;
        Map<String, String> response = new HashMap<>();

        // Checking if the input is correct
        if(!input.containsKey("COMMAND") || !input.containsKey("SENDER")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!(input.get("COMMAND").equals("GET_NOTIFICATION_NUMBER"))){
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
        String query = "SELECT COUNT(*) AS NotificationCounter FROM Message M WHERE M.emailDest = ? AND M.isSeen = FALSE;";

        // Getting messages
        try {
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, sender);
           
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next()){
                counter = resultSet.getInt("NotificationCounter");
            }
            preparedStatement.close();
            resultSet.close();
            closeDbConnection();
        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }

        response.put("NOTIFICATION_COUNTER", Integer.toString(counter));
        response.put("RESPONSE", "RETURN_NOTIFICATION_COUNTER");
        return response;
        
    }

    private static void closeDbConnection(){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }

}