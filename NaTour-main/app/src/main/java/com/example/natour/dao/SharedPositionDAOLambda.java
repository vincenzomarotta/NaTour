package com.example.natour.dao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ServiceException;
import com.example.natour.callbackinterfaces.DeleteSharedPositionResultCallback;
import com.example.natour.callbackinterfaces.GetSharedPositionListCallback;
import com.example.natour.callbackinterfaces.SetSharedPositionCallback;
import com.example.natour.daointerfaces.SharedPositionDAO;
import com.example.natour.entity.SharedPosition;
import com.example.natour.utils.CognitoSettings;
import com.example.natour.utils.UserDataPreferences;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.gms.maps.model.LatLng;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SharedPositionDAOLambda implements SharedPositionDAO {
    private static SharedPositionDAOLambda instance;

    private SharedPositionDAOLambda(){
    }

    public static SharedPositionDAOLambda getInstance(){
        if(instance == null)
            instance = new SharedPositionDAOLambda();
        return instance;
    }

    /**
     * Retrieve the list of shared locations on the specified itinerary.
     * @param id itinerary id.
     * @param context context of application.
     * @param callback callback interface.
     */
    public void getSharedPositionList(int id, Context context, GetSharedPositionListCallback callback){
        Log.d("GET_SHARED_POSITION", "Sono nella funzione");

        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "GET_SHARED_POSITION");
        outputMap.put("ITINERARY_ID", Integer.toString(id));
        outputMap.put("REQUEST_BY", new UserDataPreferences(context).getUserEmail());

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "getSharedPosition";
                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    String json = objectMapper.writeValueAsString(outputMap);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(json.getBytes()));

                    Log.d("GET_SHARED_POSITION", "Prima invoke result");

                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);

                    Log.d("GET_SHARED_POSITION", "Dopo invoke result");

                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", answer);
                    Map<String,String> inputMap = objectMapper.readValue(answer, Map.class);
                    if((inputMap.containsKey("RESPONSE")) && (inputMap.get("RESPONSE") != null)) {
                        String response = inputMap.get("RESPONSE");
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (response){
                                    case "GET_SHARED_POSITION_OK":
                                        List<SharedPosition> list = null;
                                        try {
                                            list = Arrays.asList(objectMapper.readValue(inputMap.get("JSON"), SharedPosition[].class));
                                        } catch (JsonProcessingException e) {
                                            callback.onFailure();
                                            Log.d("LAMBDA", "JsonProcessingException: " + e.getMessage());
                                        }
                                        callback.onSuccess(list);
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
                                    case "USER_NOT_EXIST":
                                        callback.onFailure();
                                        break;
                                    case "SQL_EXCEPTION":
                                        callback.onFailure();
                                        break;
                                    case "JSON_PROCESSING_ERROR":
                                        callback.onFailure();
                                        break;
                                    default:
                                        callback.onFailure();
                                        break;
                                }
                            }
                        });
                    } else {
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                callback.onFailure();
                            }
                        });
                    }
                } catch (JsonProcessingException e) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "JsonProcessingException: " + e.getMessage());
                } catch (ServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "ServiceException: " + e1.getErrorMessage());
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    /**
     * Sets the user's new shared location on the specified itinerary.
     * @param id itinerary id.
     * @param position new position of user.
     * @param context context of application.
     * @param callback callback interface.
     */
    public void setSharedPosition(int id, LatLng position, Context context, SetSharedPositionCallback callback){
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "SET_SHARED_POSITION");
        outputMap.put("ITINERARY_ID", Integer.toString(id));
        outputMap.put("REQUEST_BY", new UserDataPreferences(context).getUserEmail());
        outputMap.put("LATITUDE", Double.toString(position.latitude));
        outputMap.put("LONGITUDE", Double.toString(position.longitude));

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "setSharedPosition";
                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    String json = objectMapper.writeValueAsString(outputMap);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(json.getBytes()));
                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);

                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    String response = objectMapper.readValue(answer, String.class);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response){
                                case "SET_SHARED_POSITION_OK":
                                    callback.onSuccess();
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
                                case "USER_NOT_EXIST":
                                    callback.onFailure();
                                    break;
                                case "SQL_EXCEPTION":
                                    callback.onFailure();
                                    break;
                                default:
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
                        }
                    });
                    Log.d("LAMBDA", "JsonProcessingException: " + e.getMessage());
                } catch (ServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "ServiceException: " + e1.getErrorMessage());
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    public void deleteSharedPosition(Context context, DeleteSharedPositionResultCallback callback){
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "DEL_SHARED_POSITION");
        outputMap.put("REQUEST_BY", new UserDataPreferences(context).getUserEmail());

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "deleteSharedPosition";
                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    String json = objectMapper.writeValueAsString(outputMap);
                    Log.d("LAMBDA", "OutputMapJson -> " + json);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(json.getBytes()));
                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);

                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", "Anser -> " + answer);
                    String response = objectMapper.readValue(answer, String.class);
                    Log.d("LAMBDA", "Response -> " + response);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response){
                                case "DELETE_SHARED_POSITION_OK":
                                    callback.onSuccess();
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
                                case "USER_NOT_EXIST":
                                    callback.onFailure();
                                    break;
                                case "SQL_EXCEPTION":
                                    callback.onFailure();
                                    break;
                                default:
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
                        }
                    });
                    Log.d("LAMBDA", "JsonProcessingException: " + e.getMessage());
                } catch (ServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "ServiceException: " + e1.getErrorMessage());
                }

                return null;
            }
        };
        asyncTask.execute();
    }
}
