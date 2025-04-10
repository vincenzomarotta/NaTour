package com.example.natour.dao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;

import android.util.Log;

import androidx.annotation.NonNull;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.example.natour.callbackinterfaces.GetSearchedUsersByQueryCallback;
import com.example.natour.callbackinterfaces.GetUserResultCallback;
import com.example.natour.callbackinterfaces.UpdateUserAdminResultCallback;
import com.example.natour.daointerfaces.UserDAO;
import com.example.natour.entity.User;
import com.example.natour.utils.CognitoSettings;
import com.example.natour.utils.UserDataPreferences;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;


import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class UserDAOLambda implements UserDAO {
    private static UserDAOLambda instance;
    private static final String TAG = "UserDAOLambda";

    private UserDAOLambda() {

    }

    /**
     * It returns an instance of this class.
     * @return instance of class
     */
    public static UserDAOLambda getInstance(){
        if(instance == null)
            instance = new UserDAOLambda();
        return instance;
    }

    /**
     * It takes care of getting user datas when user correctly login into NaTour.
     * It does this through an AWS Lambda function used to call the database.
     * If the user login for the first time ever, the Lambda function will add the user into database.
     * @param context context of the application used for CognitoCredentials.
     * @param user user data if they need to be added.
     * @param callback interface for managing the exit status of the method.
     */
    public void getUser(Context context, User user, GetUserResultCallback callback){
        if(user == null) {
            callback.onFailure();
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                Log.d(TAG, "Chiamata a getUser");

                String functionName = "getUser";
                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));

                try{
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> output = new HashMap<>();
                    output.put("COMMAND", "SET_USER");
                    String userString = objectMapper.writeValueAsString(user);
                    output.put("USER", userString);

                    String payload = objectMapper.writeValueAsString(output);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(payload.getBytes()));

                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);

                    objectMapper = new ObjectMapper();
                    Map<String,String> input = objectMapper.readValue(answer, Map.class);

                    ((Activity) context).runOnUiThread(() -> {
                        if(!input.containsKey("RESPONSE")){
                            callback.onFailure();
                        }
                    });


                    Log.d("LAMBDA", "Answer -> " + input.get("RESPONSE"));
                    UserDataPreferences userDataPreferences = new UserDataPreferences(context);
                    ((Activity) context).runOnUiThread(() -> {
                        switch (input.get("RESPONSE")){
                            case "RETURN_GET_USER_OK":
                                ObjectMapper objectMapper1 = new ObjectMapper();
                                User retUser = null;
                                try {
                                    retUser = objectMapper1.readValue(input.get("RETURN_USER"), User.class);
                                } catch (JsonProcessingException e) {
                                    callback.onFailure();
                                }

                                Log.d("LAMBDA", retUser.toString());
                                Log.d(TAG,"Successo getUser.");
                                callback.onSuccess(retUser);
                                break;
                            case "UPLOAD_USER_OK":
                                Log.d(TAG,"Successo inserimento user.");
                                callback.onSuccess(user);
                                break;
                            case "REQUEST_NOT_VALID":
                            case "SQL_EXCEPTION_ADD_USER":
                            case "COMMAND_NOT_VALID":
                            case "DB_CONN_FAIL":
                            case "SQL_EXCEPTION_GET_USER":
                            case "JSON_MAPPING_EXCEPTION":
                            case "JSON_PROCESSING_EXCEPTION":
                                Log.d(TAG,"Fallimento getUser.");
                                callback.onFailure();
                                break;
                        }
                    });


                } catch (JsonProcessingException e) {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure());
                    Log.d(TAG, "JSON ERROR-> " + e.getMessage());
                } catch (AmazonServiceException e1) {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure());
                    Log.d(TAG, "SERVICE ERROR -> " + e1.getErrorMessage());
                }

                return null;
            }
        };
        asyncTask.execute();

    }

    /**
     * It takes care of promoting an user to admin.
     * It does this through an AWS Lambda function used to call the database.
     * @param context context of the application used for CognitoCredentials.
     * @param email email of the user getting a promotion.
     * @param callback interface for managing the exit status of the method.
     */
    public void setAdminTrue(Context context, String email, UpdateUserAdminResultCallback callback){
        Log.d(TAG, "Chiamata setAdminTrue.");

        if(email == null){
            callback.onFailure();
            return;
        }

        String functionName = "updateUserAdmin";
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String,String> output = new HashMap<>();
                    output.put("COMMAND", "UPDATE_USER_ADMIN");
                    output.put("EMAIL", email);

                    String payload = objectMapper.writeValueAsString(output);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(payload.getBytes()));

                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d(TAG, "Answer -> " + answer);

                    objectMapper = new ObjectMapper();
                    Map<String,String> input = objectMapper.readValue(answer, Map.class);

                    ((Activity) context).runOnUiThread(() -> {
                        if(!input.containsKey("RESPONSE")){
                            callback.onFailure();
                        }
                    });

                    ((Activity) context).runOnUiThread(() -> {
                        switch (input.get("RESPONSE")){
                            case "UPDATE_USER_ADMIN_OK":
                                UserDataPreferences userDataPreferences = new UserDataPreferences(context);
                                userDataPreferences.setUserAdminTrue();

                                Log.d(TAG, "Update riuscito.");
                                callback.onSuccess();
                                break;
                            case "REQUEST_NOT_VALID":
                            case "COMMAND_NOT_VALID":
                            case "DB_CONN_FAIL":
                            case "USER_NOT_FOUND":
                            case "SQL_EXCEPTION":
                                Log.d(TAG, "Update fallito.");
                                callback.onFailure();
                                break;
                        }
                    });

                } catch (JsonProcessingException e) {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure());
                    Log.d("LAMBDA","JSON ERROR ->" +  e.getMessage());
                } catch (AmazonServiceException e1) {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure());
                    Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                }

                return null;
            }
        };
        asyncTask.execute();

    }

    /**
     * It takes care of researching users using a string.
     * It does this through an AWS Lambda function used to call the database.
     * @param search string that represent what the user searched.
     * @param context context of the application used for CognitoCredentials.
     * @param callback interface for managing the exit status of the method.
     */
    public void getSearchedUsersByQuery(String search, Context context, @NonNull GetSearchedUsersByQueryCallback callback){
        if(search == null){
            callback.onFailure();
            return;
        }
        Log.d(TAG, "Chiamata a getSearchedUsersByQuery");
        String functionName = "getUserByQuery";
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String,String> output = new HashMap<>();
                    output.put("COMMAND", "GET_SEARCHED_USERS");
                    output.put("SEARCH", search);

                    String payload = objectMapper.writeValueAsString(output);

                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(payload.getBytes()));

                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", "Answer -> " + answer);

                    objectMapper = new ObjectMapper();
                    Map<String,String> input = objectMapper.readValue(answer, Map.class);

                    ((Activity) context).runOnUiThread(() -> {
                        if(!input.containsKey("RESPONSE")){
                            callback.onFailure();
                        }
                    });

                    ((Activity) context).runOnUiThread(() -> {
                        switch (input.get("RESPONSE")){
                            case "RETURN_USERS_OK":
                                ObjectMapper objectMapper1 = new ObjectMapper();
                                ArrayList<User> users = null;

                                try {
                                    users = objectMapper1.readValue(input.get("USERS"), objectMapper1.getTypeFactory().constructCollectionType(ArrayList.class, User.class));
                                } catch (JsonProcessingException e) {
                                    callback.onFailure();
                                }

                                Log.d(TAG, "Ricerca effettuata con successo.");
                                callback.onSuccess(users);
                                break;
                            case "NO_USER_FOUND":
                                Log.d(TAG, "Ricerca effettuata con successo ma senza risultati.");
                                callback.onResultNotFound();
                                break;
                            case "REQUEST_NOT_VALID":
                            case "COMMAND_NOT_VALID":
                            case "DB_CONN_FAIL":
                            case "SQL_EXCEPTION":
                            case "JSON_PROCESSING_EXCEPTION":
                                Log.d(TAG, "Ricerca fallita.");
                                callback.onFailure();
                                break;
                        }
                    });


                } catch (JsonProcessingException e) {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure());
                    Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                } catch (AmazonServiceException e1) {
                    ((Activity) context).runOnUiThread(() -> callback.onFailure());
                    Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                }
                return null;
            }
        };
        asyncTask.execute();


    }

}
