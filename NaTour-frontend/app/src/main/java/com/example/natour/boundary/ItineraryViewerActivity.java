package com.example.natour.boundary;

import static java.lang.Thread.sleep;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.natour.R;
import com.example.natour.callbackinterfaces.DeleteItineraryResultCallback;
import com.example.natour.callbackinterfaces.DeleteSharedPositionResultCallback;
import com.example.natour.callbackinterfaces.GetItineraryResultCallback;
import com.example.natour.callbackinterfaces.GetSharedPositionListCallback;
import com.example.natour.callbackinterfaces.ItineraryViewerFragmentReadyCallback;
import com.example.natour.callbackinterfaces.SetFavoriteResultCallback;
import com.example.natour.callbackinterfaces.SetSharedPositionCallback;
import com.example.natour.callbackinterfaces.SetToVisitResultCallback;
import com.example.natour.dao.ItineraryDAOLambda;
import com.example.natour.daointerfaces.ItineraryDAO;
import com.example.natour.utils.MyDatabase;
import com.example.natour.dao.SharedPositionDAOLambda;
import com.example.natour.daointerfaces.SimpleItineraryDAO;
import com.example.natour.entity.Itinerary;
import com.example.natour.entity.SharedPosition;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.fragment.ItineraryViewer;
import com.example.natour.fragment.ViewMaps;
import com.example.natour.utils.NetworkAvailable;
import com.example.natour.utils.SimplyStoreManager;
import com.example.natour.utils.UserDataPreferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.snackbar.Snackbar;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ItineraryViewerActivity extends AppCompatActivity {
    private static final int REFRESH_LOCATION_INTERVAL = 15;
    private static final int SHARED_POSITION_INTERVAL = 15;

    private static boolean activityRunning = true;
    private static boolean editActivityRunning = false;

    private ProgressDialog progressDialog;

    private UserDataPreferences userDataPreferences;

    private ItineraryViewer currentItineraryViewer;
    private Itinerary currentItinerary;
    private ViewMaps currentViewMaps;

    private ActivityResultLauncher<String> requestPermissionLauncher = null; //Vanno creati prima in onCreate
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private Thread sharedPositionGetterThread;
    private static boolean sharedPositionGetterThreadRunning = false;

    private boolean trackingMode = false;

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private static final String NOTIFY_CHANNEL_ID = "SHARING";
    private static final int NOTIFY_ID = 20;
    private static final int TO_VISIT = 30;
    private static final int FAVORITE = 40;
    private static final int MY_ITINERARY = 50;

    private NetworkAvailable networkAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_viewer);
        networkAvailable = new NetworkAvailable(this);
        showProgressDialog();
        initializeItineraryViewer();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        setRegisterForActivityResultForPermissionRequest();
        checkAndRequestPermissions();
        setLocationManagerAndListener();
        setNotificationManager();
        userDataPreferences = new UserDataPreferences(this);
    }

    @Override
    protected void onDestroy() {
        activityRunning = false;
        if (locationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        if (notificationManager != null)
            notificationManager.cancelAll();
        if(trackingMode)
            deleteSharedPosition();
        sharedPositionGetterThreadRunning = false;
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if ((currentItineraryViewer != null) && (editActivityRunning == true)) {
            editActivityRunning = false;
            currentItineraryViewer.showItineraryDataToScreen(currentItinerary);
        }
    }

    /**
     * Called when the activity has detected the user's press of the back
     * key. The {@link #getOnBackPressedDispatcher() OnBackPressedDispatcher} will be given a
     * chance to handle the back button before the default behavior of
     * {@link Activity#onBackPressed()} is invoked.
     *
     * @see #getOnBackPressedDispatcher()
     */
    @Override
    public void onBackPressed() {
        Log.i("UI_INTERACTION", "Premuto il pulsante indietro.");
        activityRunning = false;
        finish();
    }

    /**
     * Set the location services permission dialog.
     */
    private void setRegisterForActivityResultForPermissionRequest() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                Log.d("PERMISSION", "Granted");
                setLocationManagerAndListener();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                locationServiceInfoAlert();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.itinerary_viewer_activity_toast_location_disabled, Toast.LENGTH_LONG);
                toast.show();
            }
        });
    }

    /**
     * Set the location manager and listener for gps.
     */
    private void setLocationManagerAndListener() {
        locationManager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER))
            locationManagerAlert();
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(1000 * REFRESH_LOCATION_INTERVAL);
        locationRequest.setFastestInterval(5000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Log.d("LOCATION", "Sono in onLocationResult");
                super.onLocationResult(locationResult);
                Location newLocation = locationResult.getLastLocation();
                Log.d("LOCATION", "New coords: " + newLocation.getLatitude() + " - " + newLocation.getLongitude());
                LatLng locationToSave = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                SharedPositionDAOLambda.getInstance().setSharedPosition(currentItinerary.id,
                        locationToSave,
                        ItineraryViewerActivity.this,
                        new SetSharedPositionCallback() {
                            @Override
                            public void onSuccess() {
                                Toast.makeText(ItineraryViewerActivity.this,
                                        getResources().getString(R.string.itinerary_viewer_activity_update_share_position_success_toast),
                                        Toast.LENGTH_LONG)
                                        .show();
                                Log.i("UI_INTERACTION", "Visualizzato il toast di posizione aggiornata con successo.");
                            }

                            @Override
                            public void onFailure() {
                                Toast.makeText(ItineraryViewerActivity.this,
                                        getResources().getString(R.string.itinerary_viewer_activity_update_share_position_error_toast),
                                        Toast.LENGTH_LONG)
                                        .show();
                                Log.i("UI_INTERACTION", "Visualizzato il toast di fallimento aggiornamento posizione.");
                            }
                        });
            }
        };
    }

    /**
     * Check and set localization permissions.
     * Set the gps provider listener.
     */
    private void checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(
                this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            Log.i("UI_INTERACTION", "Visualizzata richiesta permessi di localizzazione.");
        }
    }

    /**
     * Initialize and view the ItineraryViewer fragment with the itinerary data.
     */
    private void initializeItineraryViewer() {
        currentItineraryViewer = new ItineraryViewer();
        currentItineraryViewer.setItineraryViewerFragmentReadyCallback(new ItineraryViewerFragmentReadyCallback() {
            @Override
            public void onItineraryViewerFragmentReady(ViewMaps viewMaps) {
                currentViewMaps = viewMaps;
                currentItineraryViewer.setEditButtonVisibility(userDataPreferences.checkUserIsAdmin());
                currentItineraryViewer.setDeleteButtonVisibility(userDataPreferences.checkUserIsAdmin());

                Bundle args = getIntent().getExtras();
                if ((args != null) && (args.containsKey("ID")))
                    getItinerary(args.getInt("ID"));
                else
                    getItineraryWarningAlert();
            }
        });
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.itineraryViewFragmentContainerView, currentItineraryViewer, null)
                .addToBackStack(null).commit();
        Log.i("UI_INTERACTION", "Visualizzata la schermata di visualizzazione dell'itinerario.");
    }

    private void getItinerary(int itineraryId) {
        ItineraryDAOLambda.getInstance().getItinerary(itineraryId, this, new GetItineraryResultCallback() {
            @Override
            public void onSuccess(Itinerary itinerary) {
                progressDialog.dismiss();
                Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                currentItinerary = itinerary;
                Log.d("ITINERARY", currentItinerary.toString());
                showItinerary();
                currentItineraryViewer.setFavoriteButtonIsActive(currentItinerary.isFavourite);
                Log.i("UI_INTERACTION", "Settato il pulsante dei preferiti da parte del sistema.");
                currentItineraryViewer.setToVisitButtonIsActive(currentItinerary.isToVisit);
                Log.i("UI_INTERACTION", "Settato il pulsante dei da vistitare da parte del sistema.");
                initializeGetSharingUserListThread();
            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
                Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                getItineraryWarningAlert();
            }
        });
    }

    /**
     * Show itinerary details and route on ItineraryiewerFragment.
     */
    private void showItinerary() {
        if (currentItineraryViewer != null)
            currentItineraryViewer.showItineraryDataToScreen(currentItinerary);
        if (currentViewMaps != null)
            currentViewMaps.showRoute(currentItinerary.wayPointsList);
        Log.i("UI_INTERACTION", "Visualizzato l'itinerario su mappa.");
    }

    /**
     * Toggle current location sharing mode on and off.
     *
     * @param view view
     */
    public void sharingPosition(View view) {
        Log.i("UI_INTERACTION", "Premuto il pulsante di condivisione della posizione.");
        if(networkAvailable.isNetworkAvailable()) {
            if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                trackingMode = !trackingMode;
                Log.d("SHARING_POSITION", "Valore tarckinMode -> " + Boolean.toString(trackingMode));
                currentItineraryViewer.setGpsSharingButtonActiveColor(trackingMode);
                setTrackingNotification();

                if (trackingMode) {
                    if ((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                            (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED))
                        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
                    notificationManager.notify(NOTIFY_ID, builder.build());
                } else {
                    Log.d("SHARING_POSITION", "Sono nell'else.");
                    if (locationCallback != null)
                        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
                    notificationManager.cancel(NOTIFY_ID);
                    deleteSharedPosition();
                    Log.d("SHARING_POSITION", "Sono dopo delete shared position.");
                }
            } else {
                Toast.makeText(this,
                        getString(R.string.itinerary_viewer_activity_no_location_permission_toast),
                        Toast.LENGTH_LONG).show();
                Log.i("UI_INTERACTION", "Visualizzato toast dei permessi di localizzazione mancanti.");
            }
        } else {
            Toast.makeText(this, getString(R.string.itinerary_viewer_activity_network_not_available), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Delete the shared position on db.
     */
    private void deleteSharedPosition(){
        SharedPositionDAOLambda.getInstance().deleteSharedPosition(ItineraryViewerActivity.this, new DeleteSharedPositionResultCallback() {
            @Override
            public void onSuccess() {
                Toast.makeText(ItineraryViewerActivity.this,
                        getString(R.string.itinerary_viewer_activity_delete_shared_position_success),
                        Toast.LENGTH_LONG).show();
                Log.i("UI_INTERACTION", "Visualizzato toast eliminazione posizione condivisa.");
            }

            @Override
            public void onFailure() {
                Toast.makeText(ItineraryViewerActivity.this,
                        getString(R.string.itinerary_viewer_activity_delete_shared_position_failure),
                        Toast.LENGTH_LONG).show();
                Log.i("UI_INTERACTION", "Visualizzato toast fallimento eliminazione posizione condivisa.");
            }
        });
    }

    /**
     * Set the thread for periodic retrieval of shared locations on the current itinerary.
     */
    private void initializeGetSharingUserListThread() {
        sharedPositionGetterThreadRunning = true;
        sharedPositionGetterThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
                while (sharedPositionGetterThreadRunning) {
                    SharedPositionDAOLambda.getInstance().getSharedPositionList(currentItinerary.id,
                            ItineraryViewerActivity.this,
                            new GetSharedPositionListCallback() {
                                @Override
                                public void onSuccess(@NonNull List<SharedPosition> sharedPositionList) {
                                    if (activityRunning)
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                currentViewMaps.setCurrentPositionMarkerPosition(sharedPositionList);
                                                Log.i("UI_INTERACTION", "Visualizzati eventuali aggiornamenti delle posizioni condivise.");
                                            }
                                        });
                                }

                                @Override
                                public void onFailure() {
                                    if (activityRunning) {
                                        Toast.makeText(ItineraryViewerActivity.this,
                                                getResources().getString(R.string.itinerary_viewer_activity_get_shared_position_list_error_toast),
                                                Toast.LENGTH_LONG)
                                                .show();
                                        Log.i("UI_INTERACTION", "Visualizzato il toast per il fallimento del recupero delle posizioni condivise.");
                                    }
                                }
                            });
                    try {
                        sleep(1000 * SHARED_POSITION_INTERVAL);
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                }
            }
        });
        sharedPositionGetterThread.start();
    }

    /**
     * Set the notification manager.
     */
    private void setNotificationManager() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Set the notification system.
     */
    private void setTrackingNotification() {
        //Le seguenti due righe fanno in modo da recuperare l'ultimo stato delle schermate.
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //@SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.itinerary_viewer_activity_sharing_position_notify_text))
                .setSmallIcon(R.drawable.gps_share_icon)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(pendingIntent)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFY_CHANNEL_ID, "TRACKING_STATUS", NotificationManager.IMPORTANCE_HIGH);
            builder.setChannelId(NOTIFY_CHANNEL_ID);
            notificationManager.createNotificationChannel(notificationChannel);
        }
    }

    /**
     * Starts the ItineraryCreatorManager in edit mode to modify the current itinerary.
     *
     * @param view view of current activity
     */
    public void editItinerary(View view) {
        editActivityRunning = true;
        Intent intent = new Intent(this, ItineraryCreatorManagerActivity.class);
        intent.putExtra("ACTIVITY_MODE", ItineraryCreatorManagerActivity.EDITOR_MODE);
        SimplyStoreManager.getInstance().putObject("ITINERARY_TO_EDIT", currentItinerary);
        startActivity(intent);
        Log.i("UI_INTERACTION", "Premuto il pulsante di modifica dell'itinerario.");
    }

    /**
     * Set your preferred route status.
     *
     * @param view view
     */
    public void setFavorite(View view) {
        if(networkAvailable.isNetworkAvailable()) {
            showProgressDialog();
            boolean tempNewValue = !currentItinerary.isFavourite;
            ItineraryDAOLambda.getInstance().setFavorite(currentItinerary.id, tempNewValue, this, new SetFavoriteResultCallback() {
                @Override
                public void onSuccess() {
                    progressDialog.dismiss();
                    Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                    currentItinerary.isFavourite = tempNewValue;
                    currentItineraryViewer.setFavoriteButtonIsActive(currentItinerary.isFavourite);
                    Snackbar snackbar = Snackbar.make(currentViewMaps.getView(),
                            getString(R.string.itinerary_viewer_activity_set_favorite_success_toast),
                            Snackbar.LENGTH_LONG);
                    snackbar.show();
                    Log.i("UI_INTERACTION", "Visualizzato la snackbar di successo per l'aggiornamento del preferito.");
                    setLocalDb(FAVORITE);
                }

                @Override
                public void onFailure() {
                    progressDialog.dismiss();
                    Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                    currentItineraryViewer.setFavoriteButtonIsActive(false);
                    Snackbar snackbar = Snackbar.make(currentViewMaps.getView(),
                            getString(R.string.itinerary_viewer_activity_set_favorite_failure_toast),
                            Snackbar.LENGTH_LONG);
                    snackbar.show();
                    Log.i("UI_INTERACTION", "Visualizzato la snackbar di fallimento per l'aggiornamento del preferito.");
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.itinerary_viewer_activity_network_not_available), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Set the status of the itinerary to visit.
     *
     * @param view view
     */
    public void setToVisit(View view) {
        if(networkAvailable.isNetworkAvailable()) {
            showProgressDialog();
            boolean tempNewValue = !currentItinerary.isToVisit;
            ItineraryDAOLambda.getInstance().setToVisit(currentItinerary.id, tempNewValue, this, new SetToVisitResultCallback() {
                @Override
                public void onSuccess() {
                    progressDialog.dismiss();
                    Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                    currentItinerary.isToVisit = tempNewValue;
                    currentItineraryViewer.setToVisitButtonIsActive(currentItinerary.isToVisit);
                    Snackbar snackbar = Snackbar.make(currentViewMaps.getView(),
                            getString(R.string.itinerary_viewer_activity_set_to_visit_success_toast),
                            Snackbar.LENGTH_LONG);
                    snackbar.show();
                    setLocalDb(TO_VISIT);
                    Log.i("UI_INTERACTION", "Visualizzato la snackbar di successo per l'aggiornamento del da visitare.");
                }

                @Override
                public void onFailure() {
                    progressDialog.dismiss();
                    Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                    currentItineraryViewer.setToVisitButtonIsActive(false);
                    Snackbar snackbar = Snackbar.make(currentViewMaps.getView(),
                            getString(R.string.itinerary_viewer_activity_set_to_visit_failure_toast),
                            Snackbar.LENGTH_LONG);
                    snackbar.show();
                    Log.i("UI_INTERACTION", "Visualizzato la snackbar di fallimento per l'aggiornamento del da visitare.");
                }
            });
        } else {
            Toast.makeText(this, getString(R.string.itinerary_viewer_activity_network_not_available), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Show progress dialog on screen.
     */
    private void showProgressDialog() {
        progressDialog = new ProgressDialog(this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
        Log.i("UI_INTERACTION", "Visualizzata process dialog.");
    }

    /**
     * Displays a dialog containing the itinerary update data.
     *
     * @param view view.
     */
    public void showItineraryUpdateInfoAlert(View view) {
        Log.i("UI_INTERACTION", "Visualizzata la dialog di ultima modifica itinerario.");

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss", Locale.getDefault());

        String stringToShow = getResources().getString(R.string.fragment_itinerary_viewer_itinerary_update_alert_base_message)
                .concat("   Admin: " + currentItinerary.lastModificationUser + "\n")
                .concat("   Date: " + dateFormat.format(currentItinerary.lastModificationDate));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.fragment_itinerary_viewer_itinerary_update_alert_title)
                .setMessage(stringToShow)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                })
                .create().show();
    }

    /**
     * Location system disabled warning.
     */
    private void locationManagerAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog richiesta attivazione localizzazione.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_viewer_activity_alert_title)
                .setMessage(R.string.itinerary_viewer_activity_alert_location_manager_message)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                    checkAndRequestPermissions();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante CANCEL.");
                    currentItineraryViewer.setGpsSharingButtonActiveColor(false);
                })
                .create().show();
    }

    /**
     * Location info alert.
     */
    private void locationServiceInfoAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog spiegazione richiesta localizzazione.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_viewer_activity_alert_info_location_service_title)
                .setMessage(R.string.itinerary_viewer_activity_alert_info_location_service_message)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
                        trackingPermissionErrorAlert();
                })
                .create().show();
    }

    /**
     * Location permission for tracking denied alert.
     */
    private void trackingPermissionErrorAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog spiegazione richiesta localizzazione.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            builder.setTitle(R.string.itinerary_viewer_activity_alert_tracking_title)
                    .setMessage(R.string.itinerary_viewer_activity_alert_tracking_message)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Log.i("UI_INTERACTION", "Premuto pulsante YES.");
                        checkAndRequestPermissions();
                    })
                    .setNegativeButton("Return", (dialog, which) -> {
                        Log.i("UI_INTERACTION", "Premuto pulsante RETURN.");
                        currentItineraryViewer.setGpsSharingButtonActiveColor(false);
                    })
                    .create().show();
        } else {
            builder.setTitle(R.string.itinerary_viewer_activity_alert_tracking_title)
                    .setMessage(R.string.itinerary_viewer_activity_alert_tracking_message_new_system)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                    })
                    .create().show();
        }
    }

    /**
     * Location info alert.
     */
    private void getSharedPositionListWarningAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog di recupero posizioni condivise fallito.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_viewer_activity_alert_title)
                .setMessage(R.string.itinerary_viewer_activity_get_shared_position_list_error)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                })
                .create().show();
    }

    /**
     * Getting itinerary error alert.
     */
    private void getItineraryWarningAlert() {
        Log.i("UI_INTERACTION", "Visualizzata la dialog di errore nel recupero dell'itinerrio.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_viewer_activity_alert_title)
                .setMessage(R.string.itinerary_viewer_activity_get_itinerary_error)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                    finish();
                })
                .create().show();
    }

    /**
     * Add or remove the itinerary from the chosen list from local db
     * @param type of the list
     */
    private void setLocalDb(int type) {
        MyDatabase myDatabase = MyDatabase.getInstance(ItineraryViewerActivity.this);
        SimpleItineraryDAO simpleItineraryDAO = myDatabase.simpleItineraryDAO();

        switch (type) {
            case TO_VISIT:
                if (currentItinerary.isToVisit)
                    MyDatabase.getInstance(ItineraryViewerActivity.this).simpleItineraryDAO().insertSimpleItinerary(
                            new SimpleItinerary(currentItinerary.id, currentItinerary.title, currentItinerary.description, "to_visit"));
                else
                    MyDatabase.getInstance(ItineraryViewerActivity.this).simpleItineraryDAO().takeOffFromToVisit(currentItinerary.id);
                break;
            case FAVORITE:
                if (currentItinerary.isFavourite)
                    MyDatabase.getInstance(ItineraryViewerActivity.this).simpleItineraryDAO().insertSimpleItinerary(
                            new SimpleItinerary(currentItinerary.id, currentItinerary.title, currentItinerary.description, "favorites"));
                else
                    MyDatabase.getInstance(ItineraryViewerActivity.this).simpleItineraryDAO().takeOffFromFavourite(currentItinerary.id);

                break;
        }

    }

    /**
     * Delete current itinerary.
     * @param view
     */
    public void deleteItinerary(View view) {
        deleteItineraryConfirmAlert();
    }

    /**
     * Delete itinerary confirm alert.
     */
    private void deleteItineraryConfirmAlert() {
        Log.i("UI_INTERACTION", "Visualizzata la dialog di conferma eliminazione itinerario.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_viewer_activity_confirmation_title)
                .setMessage(R.string.itinerary_viewer_activity_delete_itinerary_confirmation_text)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                    showProgressDialog();
                    int itineraryIdToDelete = currentItinerary.id;
                    ItineraryDAOLambda.getInstance().deleteItinerary(currentItinerary.id, this, new DeleteItineraryResultCallback() {
                        @Override
                        public void onSuccess() {
                            progressDialog.dismiss();
                            Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                            deleteItineraryFromLocalDb(itineraryIdToDelete);
                            Toast.makeText(ItineraryViewerActivity.this,
                                    getString(R.string.itinerary_viewer_activity_delete_itinerary_success),
                                    Toast.LENGTH_LONG)
                                    .show();
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    ItineraryViewerActivity.this.finish();
                                }
                            }, 3500);
                        }

                        @Override
                        public void onFailure() {
                            progressDialog.dismiss();
                            Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                            Toast.makeText(ItineraryViewerActivity.this,
                                    getString(R.string.itinerary_viewer_activity_delete_itinerary_failure),
                                    Toast.LENGTH_LONG)
                                    .show();
                        }
                    });
                })
                .setNegativeButton("Cancel", (dialog, which) -> {})
                .create().show();
    }

    /**
     * Delete all itinerary occurrences of itinerary in local db
     * @param id id of the chosen itinerary
     */
    public void deleteItineraryFromLocalDb(int id){
        MyDatabase myDatabase = MyDatabase.getInstance(ItineraryViewerActivity.this);
        SimpleItineraryDAO simpleItineraryDAO = myDatabase.simpleItineraryDAO();

        List<SimpleItinerary> tempList = new ArrayList<>();
        tempList = MyDatabase.getInstance(ItineraryViewerActivity.this).simpleItineraryDAO().getItinerariesById(id);

        if(tempList.size() != 0)
            MyDatabase.getInstance(ItineraryViewerActivity.this).simpleItineraryDAO().takeOffFromItinerary(id);

    }

}