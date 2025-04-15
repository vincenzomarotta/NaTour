package com.example.natour.utils;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserPool;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.regions.Regions;
import com.example.natour.R;
import com.example.natour.callbackinterfaces.GetUserResultCallback;
import com.example.natour.callbackinterfaces.LoginResultCallback;
import com.example.natour.dao.UserDAOLambda;
import com.example.natour.entity.User;

import java.util.HashMap;
import java.util.Map;


public class CognitoSettings {

    private final String USER_POOL_ID;
    private final String IDENTITY_POOL_ID;
    private final String CLIENT_ID;
    private final String CLIENT_SECRET;
    private final String DOMAIN;
    private final Regions COGNITO_REGION = Regions.US_EAST_2;

    private static CognitoSettings cognitoSettings;
    public final CognitoCachingCredentialsProvider cognitoCachingCredentialsProvider;
    private Context context;

    /**
     * Creates a new instance of CognitoSetting.
     * CognitoSetting is used to communicate with AWS Cognito services as UserPool and Identity Pool.
     * Both of them are essential to respectively login and store users and use AWS other services.
     * @param context
     */
    private CognitoSettings(Context context) {
        this.context = context;

        USER_POOL_ID = context.getString(R.string.user_pool_id);
        IDENTITY_POOL_ID = context.getString(R.string.identity_pool_id);
        CLIENT_ID = context.getString(R.string.client_id);
        CLIENT_SECRET = context.getString(R.string.client_secret);
        DOMAIN = context.getString(R.string.domain);

        cognitoCachingCredentialsProvider = new CognitoCachingCredentialsProvider(
                context, IDENTITY_POOL_ID, Regions.US_EAST_2);

    }

    /**
     * Get CognitoSetting instance if there already is one, or it will create a new instance.
     * @param context context of the application.
     * @return CognitoSetting instance.
     */
    public static CognitoSettings getCognitoInstance(Context context) {
        if (cognitoSettings != null)
            return cognitoSettings;
        else
            return (cognitoSettings = new CognitoSettings(context));
    }

    /**
     * Gets CognitoCachingCredentialsProvider.
     * @return CognitoCachingCredentialsProvider.
     */
    public CognitoCachingCredentialsProvider getCognitoCachingCredentialsProvider() {
        return cognitoCachingCredentialsProvider;
    }

    /**
     * Gets context.
     * @return context.
     */
    public Context getContext() {
        return context;
    }

    /**
     * Gets UserPool.
     * @return CognitoUSerPool.
     */
    public CognitoUserPool getUserPool() {
        return new CognitoUserPool(context, USER_POOL_ID, CLIENT_ID, CLIENT_SECRET, COGNITO_REGION);
    }

    /**
     * Sets the login for AWS Cognito Identity Pool.
     * @param userSession current user session for JWWTToken.
     * @param user current user.
     * @param callback interface for managing the exit status of the method.
     */
    public void setLogin(CognitoUserSession userSession, User user, LoginResultCallback callback) {
        UserDataPreferences userDataPreferences = new UserDataPreferences(context);

        if (user.getEmail().equals(userDataPreferences.getUserEmail())) {
            user.setName(userDataPreferences.getUserName());
            user.setSurname(userDataPreferences.getUserSurname());
            user.setAdmin(userDataPreferences.checkUserIsAdmin());
        }

        @SuppressLint("StaticFieldLeak")

        AsyncTask task = new AsyncTask() {

            @Override
            protected Object doInBackground(Object[] objects) {
                cognitoCachingCredentialsProvider.clear();
                Map<String, String> logins = new HashMap<>();
                logins.put(DOMAIN, userSession.getIdToken().getJWTToken());

                cognitoCachingCredentialsProvider.withLogins(logins).refresh();

                callback.onSuccess();

                return null;
            }
/*
            @Override
            protected void onPostExecute(Object o) {
                if(ctrl[0])
                    callback.onSuccess();
                else
                    callback.onFailure();

            }

 */
        };

        task.execute();


    }
}