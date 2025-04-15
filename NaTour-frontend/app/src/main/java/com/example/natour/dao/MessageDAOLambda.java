package com.example.natour.dao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ServiceException;
import com.example.natour.callbackinterfaces.GetMessagesResultCallback;
import com.example.natour.callbackinterfaces.GetNotificationNumberResultCallback;
import com.example.natour.callbackinterfaces.GetUserChatsResultCallback;
import com.example.natour.callbackinterfaces.SaveMessageResultCallback;
import com.example.natour.daointerfaces.MessageDAO;
import com.example.natour.entity.Message;
import com.example.natour.entity.SimpleMessage;
import com.example.natour.utils.CognitoSettings;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MessageDAOLambda implements MessageDAO {
    //LAMBDA FUNCTION FOR TAKING ROWS WE NEED
    private static MessageDAOLambda instance;

    private MessageDAOLambda(){
    }

    public static MessageDAOLambda getInstance(){
        if(instance == null)
            instance = new MessageDAOLambda();
        return instance;
    }


    //TO DO
    public void saveMessage(Message message, Context context, SaveMessageResultCallback callback){
            if (message == null) {
                callback.onFailure();
                return;
            }
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "uploadMessage";
                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    String json = objectMapper.writeValueAsString(message);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(json.getBytes()));
                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);

                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (answer) {
                                case "DB_CONN_FAIL":
                                    callback.onFailure();
                                    break;
                                case "SQL_EXCEPTION":
                                    callback.onFailure();
                                    break;
                                case "UPLOAD_MESSAGE_OK":
                                    callback.onSuccess();
                                    break;
                            }
                        }
                    });
                } catch (JsonProcessingException e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                            Log.d("LAMBDA", "JsonProcessingException: " + e.getMessage());
                        }
                    });
                } catch (ServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                            Log.d("LAMBDA", "ServiceException: " + e1.getErrorMessage());
                        }
                    });
                }
                return null;
            }
        };
        asyncTask.execute();
    }


    //TO DO
    public ArrayList<Message> getMessages(String sender, String receiver, Context context, GetMessagesResultCallback callback){
        ArrayList<Message> resultList = new ArrayList<Message>();

            if(sender == null || receiver == null){
                callback.onFailure();
                return null;
            }

            String functionName = "getMessages";
            @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
                @Override
                protected Object doInBackground(Object[] objects) {

                    AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                    lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));

                    try {
                        final ObjectMapper[] objectMapper = {new ObjectMapper()};
                        Map<String,String> output = new HashMap<>();
                        output.put("COMMAND", "GET_MESSAGES");
                        output.put("SENDER", sender);
                        output.put("RECEIVER", receiver);

                        String payload = objectMapper[0].writeValueAsString(output);

                        InvokeRequest invokeRequest = new InvokeRequest()
                                .withFunctionName(functionName)
                                .withPayload(ByteBuffer.wrap(payload.getBytes()));

                        InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
                        String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                        Log.d("LAMBDA", "Answer -> " + answer);
                        Map<String,String> input = objectMapper[0].readValue(answer, Map.class);

                        if(!input.containsKey("RESPONSE")){
                            callback.onFailure();
                            return null;
                        }

                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (input.get("RESPONSE")) {
                                    case "RETURN_MESSAGES_OK":
                                        objectMapper[0] = new ObjectMapper();
                                        ArrayList<Message> messages = new ArrayList<>();
                                        try {
                                            messages = objectMapper[0].readValue(input.get("MESSAGES"),
                                                    objectMapper[0].getTypeFactory().constructCollectionType(ArrayList.class, Message.class));
                                        } catch (JsonProcessingException e) {
                                            callback.onFailure();
                                            Log.d("LAMBDA", "JSON ERROR QUI FROCIO-> " + e.getMessage());
                                        }
                                        callback.onSuccess(messages);
                                        break;
                                    case "REQUEST_NOT_VALID":
                                        callback.onFailure();
                                        break;
                                    case "COMMAND_NOT_VALID":
                                        callback.onFailure();
                                        break;
                                    case "DB_CONN_FAIL":
                                        callback.onFailure();
                                        break;
                                    case "NO_MESSAGE_FOUND":
                                        callback.onResultNotFound();
                                        break;
                                    case "SQL_EXCEPTION":
                                        callback.onFailure();
                                        break;
                                    case "JSON_PROCESSING_EXCEPTION":
                                        callback.onFailure();
                                        break;
                                }
                            }
                        });

                    } catch (JsonProcessingException e) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure();
                                Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                            }
                        });
                    } catch (AmazonServiceException e1) {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure();
                                Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                            }
                        });
                    }
                    return null;
                }
            };
            asyncTask.execute();


        return null;
    }



    public void getUserChats(String sender, Context context, GetUserChatsResultCallback callback){
        //ArrayList<SimpleMessage> resultList = new ArrayList<SimpleMessage>();

        if(sender == null){
            callback.onFailure();
            return;
        }

        String functionName = "getUserChats";
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));

                try {
                    final ObjectMapper[] objectMapper = {new ObjectMapper()};
                    Map<String,String> output = new HashMap<>();
                    output.put("COMMAND", "GET_USER_CHAT");
                    output.put("SENDER", sender);

                    String payload = objectMapper[0].writeValueAsString(output);

                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(payload.getBytes()));

                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", "Answer -> " + answer);
                    Map<String,String> input = objectMapper[0].readValue(answer, Map.class);

                    if(!input.containsKey("RESPONSE")){
                        callback.onFailure();
                        return null;
                    }

                    ((Activity) context).runOnUiThread(() -> {
                        switch (input.get("RESPONSE")) {
                            case "RETURN_CHAT_OK":
                                objectMapper[0] = new ObjectMapper();
                                ArrayList<SimpleMessage> simpleMessages = new ArrayList<>();
                                try {
                                    if(input.get("CHATS") != null)
                                        simpleMessages = objectMapper[0].readValue(input.get("CHATS"),
                                                objectMapper[0].getTypeFactory().constructCollectionType(ArrayList.class, SimpleMessage.class));
                                } catch (JsonProcessingException e) {
                                    callback.onFailure();
                                    Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                                }
                                callback.onSuccess(simpleMessages);
                                break;
                            case "REQUEST_NOT_VALID":
                            case "COMMAND_NOT_VALID":
                            case "DB_CONN_FAIL":
                            case "NO_CHAT_FOUND":
                            case "SQL_EXCEPTION":
                            case "JSON_PROCESSING_EXCEPTION":
                                callback.onFailure();
                                break;
                        }
                    });

                } catch (JsonProcessingException e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                            Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                        }
                    });
                } catch (AmazonServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                            Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                        }
                    });
                }
                return null;
            }
        };
        asyncTask.execute();


    }

    public int getNotificationNumber(String email, Context context, GetNotificationNumberResultCallback callback) {
        if (email == null) {
            callback.onFailure();
            return 0;
        }
        String functionName = "getNotificationNumber";
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));

                try {
                    final ObjectMapper[] objectMapper = {new ObjectMapper()};
                    Map<String, String> output = new HashMap<>();
                    output.put("COMMAND", "GET_NOTIFICATION_NUMBER");
                    output.put("SENDER", email);

                    String payload = objectMapper[0].writeValueAsString(output);

                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(payload.getBytes()));

                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", "Answer -> " + answer);
                    Map<String, String> input = objectMapper[0].readValue(answer, Map.class);

                    if (!input.containsKey("RESPONSE")) {
                        callback.onFailure();
                        return null;
                    }

                    ((Activity) context).runOnUiThread(() -> {
                        switch (input.get("RESPONSE")) {
                            case "RETURN_NOTIFICATION_COUNTER":
                                callback.onSuccess(Integer.valueOf(input.get("NOTIFICATION_COUNTER")));
                                break;
                            case "REQUEST_NOT_VALID":
                            case "COMMAND_NOT_VALID":
                            case "DB_CONN_FAIL":
                            case "SQL_EXCEPTION":
                                callback.onFailure();
                                break;
                        }
                    });

                } catch (JsonProcessingException e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                            Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                        }
                    });
                } catch (AmazonServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                            Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                        }
                    });
                } catch (NullPointerException e2){ //AGGIUNTO DA VERIFICARE
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                            Log.d("LAMBDA", "SERVICE ERROR -> " + e2.getMessage());
                        }
                    });
                }
                return 0;
            }
        };
        asyncTask.execute();
        return 0;
    }
}
