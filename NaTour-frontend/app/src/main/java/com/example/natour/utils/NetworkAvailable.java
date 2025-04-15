package com.example.natour.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import androidx.annotation.NonNull;

import com.example.natour.R;
import com.example.natour.boundary.LoginActivity;

public class NetworkAvailable {
    Context context;

    /**
     * Creates NetworkAvailable instance, used to check if there is active connection.
     * @param context context of the application used to get SystemService.
     */
    public NetworkAvailable(@NonNull Context context){
        this.context = context;
    }

    /**
     * Checks if there is active connection.
     * @return true if there is connection, false if there is not.
     */
    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    /**
     * Creates error alert if there is no connection.
     */
    public void createAlertNoInternet(){
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.check_connection))
                .setMessage(context.getString(R.string.no_connection_message))
                .setIcon(context.getDrawable(R.drawable.error_icon))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }
}
