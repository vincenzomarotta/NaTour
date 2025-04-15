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
        boolean ctrl = false;
        int rows = 0;
        Map<String, String> response = new HashMap<>();

        // Checking if the input is correct
        if(!input.containsKey("COMMAND") || !input.containsKey("USER") || !input.containsKey("ID")){
            response.put("RESPONSE", "REQUEST_NOT_VALID");
            return response;
        }

        if(!(input.get("COMMAND").equals("DELETE_ITINERARY"))){
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
        String user = input.get("USER");
        
        String queryCTRL = "SELECT U.isAdmin, COUNT(*) AS rowsCounter FROM User U WHERE U.email = ?;";
        // Getting isADMIN
        try {
            PreparedStatement preparedStatement = mySql.prepareStatement(queryCTRL);
            preparedStatement.setString(1, user);
           
            ResultSet resultSet = preparedStatement.executeQuery();
            
            while(resultSet.next()){
                rows = resultSet.getInt("rowsCounter");
                if(rows<1){
                   preparedStatement.close();
                   resultSet.close();
                   closeDbConnection();
                   response.put("RESPONSE","USER_NOT_EXIST");
                   return response;
                }
                ctrl = resultSet.getBoolean("isAdmin");
            }

            preparedStatement.close();
            resultSet.close();
        } catch (SQLException e) {
            closeDbConnection();
            response.put("RESPONSE", "SQL_EXCEPTION");
            return response;
        }
        
        int id = Integer.valueOf(input.get("ID"));
        String query = "DELETE FROM Itinerary I WHERE I.id = ?;";
        if(ctrl){
            try{
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();
            preparedStatement.close();
            closeDbConnection();
            }catch(SQLException e){
                closeDbConnection();
                response.put("RESPONSE","SQL_EXCEPTION");
                return response;
            }
        }else{
            response.put("RESPONSE","USER_NOT_VALID");
            return response;
        }


        response.put("RESPONSE", "DELETE_ITINERARY_OK");
        return response;
        
    }

    private static void closeDbConnection(){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }

}
