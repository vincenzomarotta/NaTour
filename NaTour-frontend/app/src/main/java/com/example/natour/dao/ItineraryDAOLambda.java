package com.example.natour.dao;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import androidx.annotation.NonNull;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ServiceException;
import com.example.natour.callbackinterfaces.DeleteItineraryResultCallback;
import com.example.natour.callbackinterfaces.GetItineraryResultCallback;
import com.example.natour.callbackinterfaces.GetRandomItinerariesResultCallback;
import com.example.natour.callbackinterfaces.GetSearchedItinerariesByQueryCallback;
import com.example.natour.callbackinterfaces.GetUserListCallback;
import com.example.natour.callbackinterfaces.SaveItineraryResultCallback;
import com.example.natour.callbackinterfaces.SetFavoriteResultCallback;
import com.example.natour.callbackinterfaces.SetToVisitResultCallback;
import com.example.natour.callbackinterfaces.UpdateItineraryResultCallback;
import com.example.natour.daointerfaces.ItineraryDAO;
import com.example.natour.entity.Itinerary;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.utils.CognitoSettings;
import com.example.natour.utils.QueryGenerator;
import com.example.natour.utils.UserDataPreferences;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ItineraryDAOLambda implements ItineraryDAO {
    private static ItineraryDAOLambda instance;

    private ItineraryDAOLambda() {
    }

    /**
     * It returns an instance of this class.
     * @return instance of class
     */
    public static ItineraryDAOLambda getInstance() {
        if (instance == null) {
            instance = new ItineraryDAOLambda();
        }
        return instance;
    }

    /**
     * It takes care of saving an itinerary on the db through a call to an AWS Lambda function.
     * Context is used to retrieve the AWS Cognito instance to use authentication credentials in the call.
     * @param itinerary itinerary to save.
     * @param context context of application.
     * @param callback interface for managing the exit status of the method.
     */
    public void saveItinerary(Itinerary itinerary, Context context, @NonNull SaveItineraryResultCallback callback) {
        if (itinerary == null) {
            callback.onFailure();
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "uploadItinerary";
                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    String json = objectMapper.writeValueAsString(itinerary);
                    Log.d("LAMBDA", json);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(json.getBytes()));
                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);

                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", "Answer -> " + answer);
                    Map<String,String> inputMap = objectMapper.readValue(answer, Map.class);
                    String response = inputMap.get("RESPONSE");
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response) {
                                case "DB_CONN_FAIL":
                                    callback.onFailure();
                                    break;
                                case "SQL_EXCEPTION":
                                    callback.onFailure();
                                    break;
                                case "UPLOAD_ITINERARY_OK":
                                    callback.onSuccess(Integer.parseInt(inputMap.get("NEW_ID")));
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
                } catch (IOException e2){
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "ServiceException: " + e2.getMessage());
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    /**
     * It takes care of updating a modified itinerary on the db through a call to an AWS Lambda function.
     * Context is used to retrieve the AWS Cognito instance to use authentication credentials in the call.
     * @param itinerary itinerary to update.
     * @param context context of application.
     * @param callback interface for managing the exit status of the method.
     */
    public void updateItinerary(Itinerary itinerary, Context context, @NonNull UpdateItineraryResultCallback callback) {
        if(itinerary == null){
            callback.onFailure();
            return;
        }

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "updateItinerary";
                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));
                ObjectMapper objectMapper = new ObjectMapper();

                try {
                    String json = objectMapper.writeValueAsString(itinerary);
                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(json.getBytes()));
                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);

                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", "Answer -> " + answer);
                    String response = objectMapper.readValue(answer, String.class);
                    Log.d("LAMBDA", "Response -> " + response);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response) {
                                case "UPDATE_ITINERARY_OK":
                                    callback.onSuccess();
                                    break;
                                case "DB_CONN_FAIL":
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

    /**
     * It takes care of retrieving a itinerary from the db through an AWS Lambda call.
     * Context is used to retrieve the AWS Cognito instance to use authentication credentials in the call.
     * The context is also used to retrieve an instance of the UserDataPreferences class from which to retrieve user data.
     * @param id identification of the Itinerary.
     * @param context context of application.
     * @param callback interface for managing the exit status of the method.
     */
    public void getItinerary(int id, Context context, @NonNull GetItineraryResultCallback callback) {
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "DOWNLOAD_ITINERARY");
        outputMap.put("ITINERARY_ID", Integer.toString(id));
        outputMap.put("REQUEST_BY", new UserDataPreferences(context).getUserEmail());

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "downloadItinerary";
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
                    Log.d("LAMBDA", answer);
                    Map<String,String> inputMap = objectMapper.readValue(answer, Map.class);
                    if((inputMap.containsKey("RESPONSE")) && (inputMap.get("RESPONSE") != null)){
                        String response = inputMap.get("RESPONSE");
                        Log.d("LAMBDA", response);
                        Log.d("LAMBDA", "JSON -> " + inputMap.get("JSON"));
                        ((Activity) context).runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                switch (response){
                                    case "DOWNLOAD_ITINERARY_OK":
                                        Itinerary itinerary = null;
                                        Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd hh:mm:ss").create();
                                        itinerary = gson.fromJson(inputMap.get("JSON"), Itinerary.class);
                                        callback.onSuccess(itinerary);
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
                                    case "ITINERARY_NOT_EXIST":
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
                    }
                } catch (JsonProcessingException e) {
                    callback.onFailure();
                    Log.d("LAMBDA", "JsonProcessingException: " + e.getMessage());
                } catch (ServiceException e1) {
                    callback.onFailure();
                    Log.d("LAMBDA", "ServiceException: " + e1.getErrorMessage());
                }
                return null;
            }
        };
        asyncTask.execute();
    }

    /**
     * It takes care of deleting a route from the database via an AWS Lambda call.
     * Context is used to retrieve the AWS Cognito instance to use authentication credentials in the call.
     * The context is also used to retrieve an instance of the UserDataPreferences class from which to retrieve user data.
     * @param id identification of the Itinerary.
     * @param context context of application.
     * @param callback interface for managing the exit status of the method.
     */
    public void deleteItinerary(int id, Context context, @NonNull DeleteItineraryResultCallback callback) {
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "DELETE_ITINERARY");
        outputMap.put("ID", Integer.toString(id));
        outputMap.put("USER", new UserDataPreferences(context).getUserEmail());

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "deleteItinerary";
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
                    Log.d("LAMBDA", "Answer -> " + answer);
                    Map<String,String> inputMap = objectMapper.readValue(answer, Map.class);
                    String response = inputMap.get("RESPONSE");
                    Log.d("LAMBDA", "Response -> " + response);
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response) {
                                case "DELETE_ITINERARY_OK":
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
                                case "USER_NOT_VALID":
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

    /**
     * It takes care of managing the "favorite" status of an itinerary on the db through a call to an AWS Lambda function.
     * Context is used to retrieve the AWS Cognito instance to use authentication credentials in the call.
     * The context is also used to retrieve an instance of the UserDataPreferences class from which to retrieve user data.
     * @param id identification of itinerary.
     * @param value boolean value that identifies the status of favorite.
     * @param context context of application.
     * @param callback interface for managing the exit status of the method.
     */
    public void setFavorite(int id, boolean value, Context context, @NonNull SetFavoriteResultCallback callback) {
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "SET_LIST");
        outputMap.put("ITINERARY_ID", Integer.toString(id));
        outputMap.put("REQUEST_BY", new UserDataPreferences(context).getUserEmail());
        outputMap.put("FAVORITE", Boolean.toString(value));

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "setFavorite";
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
                    Log.d("LAMBDA", "Answer -> " + answer);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response) {
                                case "FAVORITE=true":
                                    callback.onSuccess();
                                    break;
                                case "FAVORITE=false":
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
                                case "SQL_EXCEPTION":
                                    callback.onFailure();
                                    break;
                                case "USER_NOT_EXIST":
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


    /**
     * It takes care of managing the "to visit" status of an itinerary on the db through a call to an AWS Lambda function.
     * Context is used to retrieve the AWS Cognito instance to use authentication credentials in the call.
     * The context is also used to retrieve an instance of the UserDataPreferences class from which to retrieve user data.
     * @param id identification of itinerary.
     * @param value boolean value that identifies the status of to visit.
     * @param context context of application.
     * @param callback interface for managing the exit status of the method.
     */
    public void setToVisit(int id, boolean value, Context context, @NonNull SetToVisitResultCallback callback) {
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "SET_LIST");
        outputMap.put("ITINERARY_ID", Integer.toString(id));
        outputMap.put("REQUEST_BY", new UserDataPreferences(context).getUserEmail());
        outputMap.put("TO_VISIT", Boolean.toString(value));

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "setToVisit";
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
                    Log.d("LAMBDA", "Answer -> " + answer);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (response) {
                                case "TO_VISIT=true":
                                    callback.onSuccess();
                                    break;
                                case "TO_VISIT=false":
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
                                case "SQL_EXCEPTION":
                                    callback.onFailure();
                                    break;
                                case "USER_NOT_EXIST":
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

    /**
     * It takes care to search an itinerary on db through a call to an AWS Lambda function.
     * The itinerary is searched using a string and some optional filters chosen by the user.
     * Context is used to get an instance of AWS Cognito Identity Provider.
     * @param search string to search in db.
     * @param context context of the application.
     * @param filter filters applied to the research.
     * @param callback interface for managing the exit status of the method.
     */
    public void getSearchedItinerariesByQuery(String search, Context context, HashMap<String, String> filter,
                                                @NonNull GetSearchedItinerariesByQueryCallback callback) {
        ArrayList<SimpleItinerary> resultList = new ArrayList<>();

        if(search == null){
            callback.onFailure();
            return;
        }

        String functionName = "getItineraryByQuery";
        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {

                AWSLambdaClient lambdaClient = new AWSLambdaClient(CognitoSettings.getCognitoInstance(context).cognitoCachingCredentialsProvider);
                lambdaClient.setRegion(Region.getRegion(Regions.US_EAST_2));

                QueryGenerator queryGenerator = new QueryGenerator(search);
                String query = queryGenerator.createFilterQuery(filter);

                try {
                    ObjectMapper objectMapper = new ObjectMapper();
                    Map<String, String> output = new HashMap<>();
                    output.put("COMMAND", "GET_SEARCHED_ITINERARIES");
                    output.put("QUERY", query);

                    String payload = objectMapper.writeValueAsString(output);
                    Log.d("LAMBDA_SEARCH", payload);

                    InvokeRequest invokeRequest = new InvokeRequest()
                            .withFunctionName(functionName)
                            .withPayload(ByteBuffer.wrap(payload.getBytes()));

                    InvokeResult invokeResult = lambdaClient.invoke(invokeRequest);
                    String answer = new String(invokeResult.getPayload().array(), StandardCharsets.UTF_8);
                    Log.d("LAMBDA", "Answer -> " + answer);

                    objectMapper = new ObjectMapper();
                    Map<String, String> input = objectMapper.readValue(answer, Map.class);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!input.containsKey("RESPONSE")){
                                callback.onFailure();
                            }
                        }
                    });

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (input.get("RESPONSE")){
                                case "RETURN_ITINERARIES_OK":
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    ArrayList<Itinerary> tempList = null;
                                    try {
                                        tempList = objectMapper.readValue(input.get("ITINERARIES"),
                                                objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Itinerary.class));
                                    } catch (JsonProcessingException e) {
                                        callback.onFailure();
                                    }
                                    for(int i=0; i< tempList.size(); i++){
                                        resultList.add(new SimpleItinerary
                                                (tempList.get(i).id, tempList.get(i).title, tempList.get(i).description));
                                    }
                                    callback.onSuccess(resultList);
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
                                case "NO_ITINERARY_FOUND":
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
                        }
                    });
                    Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                } catch (ServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                }
                return null;
            }
        };
        asyncTask.execute();

    }

    /**
     * It takes care of getting all user lists from db through a call to an AWS Lambda function.
     * Using the email, the function get all the itineraries associated to the account.
     * Context is used to get an instance of AWS Cognito Identity Provider.
     * @param email email fo the user.
     * @param context context of the application.
     * @param callback interface for managing the exit status of the method.
     */
    public void getUserList(String email, Context context, GetUserListCallback callback){
        Log.d("LAMBDA_GET_LISTS", "getUserList");
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "GET_LISTS");
        Log.d("LAMBDA GETUSERLIST", email);
        outputMap.put("REQUEST_BY", email);

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "getLists";
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
                    Map<String, String> input = objectMapper.readValue(answer, Map.class);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!input.containsKey("RESPONSE")){
                                callback.onFailure();
                            }
                        }
                    });


                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (input.get("RESPONSE")) {
                                case "GET_LIST_OK":
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    ArrayList<Itinerary> tempList = null;

                                    ArrayList<SimpleItinerary> visitList = new ArrayList<>();
                                    Log.d("LAMBDA_GET_LISTS", String.valueOf(input.get("TO_VISIT")));
                                    if(input.get("TO_VISIT") != null) {
                                        try {
                                            tempList = objectMapper.readValue(input.get("TO_VISIT"),
                                                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Itinerary.class));
                                        } catch (JsonProcessingException e) {
                                            callback.onFailure();
                                        }

                                        for(int i=0; i< tempList.size(); i++){
                                            visitList.add(new SimpleItinerary
                                                    (tempList.get(i).id, tempList.get(i).title, tempList.get(i).description, "to_visit"));
                                        }

                                        tempList.clear();
                                    }

                                    objectMapper = new ObjectMapper();

                                    ArrayList<SimpleItinerary> favoriteList = new ArrayList<>();
                                    Log.d("LAMBDA_GET_LISTS", String.valueOf(input.get("FAVORITE")));
                                    if(input.get("FAVORITE") != null) {
                                        try {
                                            tempList = objectMapper.readValue(input.get("FAVORITE"),
                                                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Itinerary.class));
                                        } catch (JsonProcessingException e) {
                                            callback.onFailure();
                                        }

                                        for(int i=0; i< tempList.size(); i++){
                                            favoriteList.add(new SimpleItinerary
                                                    (tempList.get(i).id, tempList.get(i).title, tempList.get(i).description, "favorites"));
                                        }

                                        tempList.clear();
                                    }

                                    objectMapper = new ObjectMapper();

                                    ArrayList<SimpleItinerary> myItineraryList = new ArrayList<>();
                                    Log.d("LAMBDA_GET_LISTS", String.valueOf(input.get("MY_ITINERARY")));
                                    if(input.get("MY_ITINERARY") != null) {
                                        try {
                                            tempList = objectMapper.readValue(input.get("MY_ITINERARY"),
                                                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Itinerary.class));
                                        } catch (JsonProcessingException e) {
                                            callback.onFailure();
                                        }

                                        for (int i = 0; i < tempList.size(); i++) {
                                            myItineraryList.add(new SimpleItinerary
                                                    (tempList.get(i).id, tempList.get(i).title, tempList.get(i).description, "my_itinerary"));
                                        }
                                    }

                                    callback.onSuccess(visitList, favoriteList, myItineraryList);
                                    break;
                                case "REQUEST_NOT_VALID":
                                case "COMMAND_NOT_VALID":
                                case "DB_CONN_FAIL":
                                case "USER_NOT_EXIST":
                                case "SQL_EXCEPTION":
                                case "VISIT_JSON_PROCESSING_EXCEPTION":
                                case "VISIT_SQL_EXCEPTION":
                                case "FAVORITE_JSON_PROCESSING_EXCEPTION":
                                case "MY_ITINERARY_JSON_PROCESSING_EXCEPTION":
                                case "MY_ITINERARY_SQL_EXCEPTION":
                                    Log.d("LAMBDA_GET_LISTS", input.get("RESPONSE"));
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
                    Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                } catch (ServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                }
                return null;
            }
        };
        asyncTask.execute();

    }

    public void getRandomItineraries(Context context, GetRandomItinerariesResultCallback callback){
        Map<String,String> outputMap = new HashMap<>();
        outputMap.put("COMMAND", "GET_RANDOM_ITINERARIES");

        @SuppressLint("StaticFieldLeak") AsyncTask asyncTask = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                String functionName = "getRandomItineraries";
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
                    Map<String, String> input = objectMapper.readValue(answer, Map.class);

                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(!input.containsKey("RESPONSE")){
                                callback.onFailure();
                            }
                        }
                    });


                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            switch (input.get("RESPONSE")) {
                                case "RETURN_RANDOM_OK":
                                    ObjectMapper objectMapper = new ObjectMapper();
                                    ArrayList<Itinerary> tempList = null;

                                    ArrayList<SimpleItinerary> randomItineraries = new ArrayList<>();
                                    Log.d("LAMBDA_RANDOM_ITINERARIES", String.valueOf(input.get("RANDOM_ITINERARIES")));
                                    if(input.get("RANDOM_ITINERARIES") != null) {
                                        try {
                                            tempList = objectMapper.readValue(input.get("RANDOM_ITINERARIES"),
                                                    objectMapper.getTypeFactory().constructCollectionType(ArrayList.class, Itinerary.class));
                                        } catch (JsonProcessingException e) {
                                            callback.onFailure();
                                        }

                                        for(int i=0; i< tempList.size(); i++){
                                            randomItineraries.add(new SimpleItinerary
                                                    (tempList.get(i).id, tempList.get(i).title, tempList.get(i).description));
                                        }

                                        tempList.clear();
                                    }

                                    callback.onSuccess(randomItineraries);
                                    break;
                                case "COMMAND_NOT_VALID":
                                case "DB_CONN_FAIL":
                                case "SQL_EXCEPTION":
                                case "NO_ITINERARIES_FOUND":
                                case "JSON_PROCESSING_EXCEPTION":
                                    Log.d("LAMBDA_RANDOM_ITINERARIES", input.get("RESPONSE"));
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
                    Log.d("LAMBDA", "JSON ERROR -> " + e.getMessage());
                } catch (ServiceException e1) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            callback.onFailure();
                        }
                    });
                    Log.d("LAMBDA", "SERVICE ERROR -> " + e1.getErrorMessage());
                }
                return null;
            }
        };
        asyncTask.execute();

    }



}
