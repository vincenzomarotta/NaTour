package com.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

/**
 * Lambda function entry point. You can change to use other pojo type or implement
 * a different RequestHandler.
 *
 * @see <a href=https://docs.aws.amazon.com/lambda/latest/dg/java-handler.html>Lambda Java Handler</a> for more information
 */
public class App implements RequestHandler<Message, String> {
    private static Connection mySql;
    private Message message;

    @Override
    public String handleRequest(final Message input, final Context context) {
        this.message = input;
        // Connessione al database
        try {
            mySql = DriverManager.getConnection("jdbc:mysql://natour2021-22.c33rkihh6dsa.us-east-2.rds.amazonaws.com:3306/NaTour",
            "admin",
            "natour123");
        } catch (SQLException e) {
            return "DB_CONN_FAIL";
        }


        // Inserimento dati del nuovo messaggio
        try {
            String query = "INSERT INTO Message(emailDest,emailSend,message,date,isSeen) "
                +
                "VALUES (?,?,?,?,?);";
            PreparedStatement preparedStatement = mySql.prepareStatement(query);
            preparedStatement.setString(1, message.receiver);
            preparedStatement.setString(2, message.sender);
            preparedStatement.setString(3, message.body);
            preparedStatement.setTimestamp(4, message.date);
            preparedStatement.setBoolean(5, message.isSeen);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            closeDbConnection();
            return "SQL_EXCEPTION";
        }

    

        // Chiusura della connessione al database
        closeDbConnection();

        // Invio responso
        return "UPLOAD_MESSAGE_OK";
    }

    private static void closeDbConnection(){
        try {
            mySql.close();
        } catch (SQLException e) {
        }
    }
}
