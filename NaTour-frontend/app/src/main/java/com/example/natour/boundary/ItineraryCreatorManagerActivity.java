package com.example.natour.boundary;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.natour.R;
import com.example.natour.callbackinterfaces.SaveItineraryResultCallback;
import com.example.natour.callbackinterfaces.UpdateItineraryResultCallback;
import com.example.natour.dao.ItineraryDAOLambda;
import com.example.natour.utils.MyDatabase;
import com.example.natour.daointerfaces.SimpleItineraryDAO;
import com.example.natour.entity.Itinerary;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.fragment.EditableMaps;
import com.example.natour.fragment.GpxItineraryChooser;
import com.example.natour.fragment.ItineraryCreationChooser;
import com.example.natour.fragment.ItineraryDetailsSetter;
import com.example.natour.utils.GeoUtils;
import com.example.natour.utils.NetworkAvailable;
import com.example.natour.utils.SimplyStoreManager;
import com.example.natour.utils.UserDataPreferences;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

import org.xmlpull.v1.XmlPullParserException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Route;
import io.ticofab.androidgpxparser.parser.domain.RoutePoint;

public class ItineraryCreatorManagerActivity extends AppCompatActivity {
    public static final int CREATOR_MODE = 0;
    public static final int EDITOR_MODE = 1;
    private int activityMode = 0;

    private static final int REFRESH_LOCATION_INTERVAL = 5;
    private static final int FAST_REFRESH_LOCATION_INTERVAL = 3;
    private static final double MINIMUM_DISTANCE_BETWEEN_TWO_POINTS = 10d;

    private boolean trackingMode = false;
    private boolean recordingStatus = false;

    private ProgressDialog progressDialog;

    private UserDataPreferences userDataPreferences;

    private ActivityResultLauncher<String> requestPermissionLauncher = null; //Vanno creati prima in onCreate
    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;

    private ActivityResultLauncher<Intent> fileBrowserLauncher = null; //Vanno creati prima in onCreate

    private Itinerary currentItinerary = null;
    private EditableMaps currentEditableMap = null;
    private ItineraryDetailsSetter currentItineraryDetailSetter = null;
    private Uri gpxFileUri = null;
    private Gpx parsedGpx = null;
    private Route currentRoute = null;
    private GpxItineraryChooser currentGpxItineraryChooser = null;

    private NotificationCompat.Builder builder;
    private NotificationManager notificationManager;
    private static final String NOTIFY_CHANNEL_ID = "TRACKING";
    private static final int NOTIFY_ID = 10;

    private NetworkAvailable networkAvailable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_itinerary_creator_manager);

        networkAvailable = new NetworkAvailable(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        setRegisterForActivityResultForPermissionRequest();
        setRegisterForActivityResultForBrowserLauncher();
        checkAndRequestPermissions();
        setLocationManagerAndListener();
        setNotificationManager();
        userDataPreferences = new UserDataPreferences(ItineraryCreatorManagerActivity.this);
        Log.d("ITINERARY_CREATION", "Sono prima dello switch");
        if (userDataPreferences.checkUserIsAdmin()) {
            setArguments();
            switch (activityMode) {
                case CREATOR_MODE:
                    openItineraryCreationChooser();
                    break;
                case EDITOR_MODE:
                    editorProcedure();
                    break;
            }
        } else
            openItineraryCreationChooser();
    }

    @Override
    protected void onDestroy() {
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        if (notificationManager != null)
            notificationManager.cancelAll();

        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.i("UI_INTERACTION", "Premuto il pulsante indietro.");
        if (fusedLocationProviderClient != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        super.onBackPressed();
    }

    /**
     * Set the activity arguments if they're present.
     */
    private void setArguments() {
        Bundle args = getIntent().getExtras();
        if (args != null) {
            if (args.containsKey("ACTIVITY_MODE"))
                activityMode = getIntent().getIntExtra("ACTIVITY_MODE", 0);
        }
    }

    /**
     * Set the location services permission dialog.
     */
    private void setRegisterForActivityResultForPermissionRequest() {
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
            if (isGranted) {
                setLocationManagerAndListener();
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                locationServiceInfoAlert();
            } else {
                Toast toast = Toast.makeText(getApplicationContext(), R.string.itinerary_creator_manager_toast_location_disabled, Toast.LENGTH_LONG);
                toast.show();
                Log.i("UI_INTERACTION", "Visualizzato il toast di localizzazione non attiva.");
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
        locationRequest.setFastestInterval(1000 * FAST_REFRESH_LOCATION_INTERVAL);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                Log.d("LOCATION", "Sono in onLocationResult");
                super.onLocationResult(locationResult);
                Location newLocation = locationResult.getLastLocation();
                Log.d("LOCATION", "New coords: " + newLocation.getLatitude() + " - " + newLocation.getLongitude());
                LatLng locationToSave = new LatLng(newLocation.getLatitude(), newLocation.getLongitude());
                if ((trackingMode == true) && (recordingStatus == true))
                    if (currentItinerary.wayPointsList.size() > 0) {
                        double oldLatitude = currentItinerary.wayPointsList.getLast().latitude;
                        double oldLongitude = currentItinerary.wayPointsList.getLast().longitude;
                        Location lastRegisteredLocation = new Location("OLD_LAST_COORDS");
                        lastRegisteredLocation.setLatitude(oldLatitude);
                        lastRegisteredLocation.setLongitude(oldLongitude);
                        double distance = lastRegisteredLocation.distanceTo(newLocation);
                        if (distance >= MINIMUM_DISTANCE_BETWEEN_TWO_POINTS)
                            currentEditableMap.addWayPointAndMoveView(locationToSave);
                    } else
                        currentEditableMap.addWayPointAndMoveView(locationToSave);
            }
        };
    }

    /**
     * Check and set localization permissions.
     * Set the gps provider listener.
     */
    private void checkAndRequestPermissions() {
        Log.d("PERMISSION", "Sono in check.");
        if (ContextCompat.checkSelfPermission(
                this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_DENIED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            Log.i("UI_INTERACTION", "Visualizzata richiesta permessi di localizzazione.");
        }
    }

    /**
     * Set the file browser launcher.
     */
    private void setRegisterForActivityResultForBrowserLauncher() {
        fileBrowserLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        if (result.getData() != null)
                            gpxFileUri = result.getData().getData();
                        Log.d("IntentResult", gpxFileUri.toString());
                        if (loadGpxFile())
                            gpxItineraryChooser();
                    } else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                        gpxFileUri = null;
                        Log.d("IntentResult", "CANCELLED");
                    } else {
                        gpxFileUri = null;
                        Log.d("IntentResult", "ELSE");
                    }
                });
    }

    /**
     * Set the notification manager.
     */
    private void setNotificationManager() {
        notificationManager = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
    }

    /**
     * Set the itinerary owner.
     */
    private void setItineraryOwner() {
        if (activityMode == CREATOR_MODE) {
            currentItinerary.ownerEmail = userDataPreferences.getUserEmail();
        }
    }

    /**
     * Set the travel time on itinerary object.
     */
    private void setTravelTime() {
        currentItinerary.duration = new GeoUtils().getTravelTime(currentItinerary.wayPointsList, GeoUtils.WALK);
    }

    /**
     * Set the travel length on itinerary object.
     */
    private void setTravelLength() {
        currentItinerary.length = new GeoUtils().getRouteLength(currentItinerary.wayPointsList);
    }

    /**
     * Set the last modification user.
     */
    private void setLastModificationUser() {
        currentItinerary.lastModificationUser = userDataPreferences.getUserEmail();
    }

    /**
     * Set the last modification date.
     */
    private void setLastModificationDateTime() {
        currentItinerary.lastModificationDate = Calendar.getInstance().getTime();
    }

    /**
     * Opens the fragment for choosing the route points input method.
     */
    private void openItineraryCreationChooser() {
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .add(R.id.itineraryCreatorManagerFragmentContainerView, ItineraryCreationChooser.class, null)
                .commit();
    }

    /**
     * Procedure for setting itinerary data.
     *
     * @param view android view
     */
    public void setItineraryDetails(View view) {
        if (currentItinerary == null)
            throw new NullPointerException("currentItinerary can't be null.");

        if (currentItinerary.wayPointsList == null)
            throw new NullPointerException("currentItinerary.wayPointsList can't be null.");

        if ((trackingMode) && (recordingStatus)) {
            recordingStatus = false;
            currentEditableMap.setRecordingStatus(false);
        }

        if (currentItinerary.wayPointsList.size() == 0) {
            wayPointsListSizeAlert();
        } else {
            notificationManager.cancel(NOTIFY_ID);
            switch (activityMode) {
                case CREATOR_MODE:
                    setItineraryOwner();
                    setTravelTime();
                    setTravelLength();
                    setLastModificationUser();
                    setLastModificationDateTime();
                    break;
                case EDITOR_MODE:
                    setLastModificationUser();
                    setLastModificationDateTime();
                    break;
            }
            initializeDetailsSetter();
            openItineraryDetailsSetter();
        }
    }

    /**
     * Procedure for initializing the route data entry screen.
     */
    private void initializeDetailsSetter() {
        currentItineraryDetailSetter = new ItineraryDetailsSetter();

        String timeTravel = new GeoUtils().convertSecondToFormattedString(currentItinerary.duration);
        String routeLength = Double.toString(currentItinerary.length);
        routeLength = routeLength.substring(0, routeLength.indexOf("."));
        routeLength = routeLength.concat(" m.");

        Bundle args = new Bundle();
        args.putString("DURATION", timeTravel);
        args.putString("LENGTH", routeLength);
        if (activityMode == EDITOR_MODE) {
            args.putString("TITLE", currentItinerary.title);
            args.putString("STATE", currentItinerary.state);
            args.putString("REGION", currentItinerary.region);
            args.putString("CITY", currentItinerary.city);
            args.putBoolean("ACCESSIBILITY", currentItinerary.accessibility);
            args.putBoolean("IS_PRIVATE", currentItinerary.isPrivate);
            args.putFloat("DIFFICULTY", currentItinerary.difficulty);
            args.putString("DESCRIPTION", currentItinerary.description);
        }
        currentItineraryDetailSetter.setArguments(args);
    }

    /**
     * Opens the route data entry screen.
     */
    private void openItineraryDetailsSetter() {
        Log.i("UI_INTERACTION", "Aperta schermata per il settagio dei dettagli dell'itinerario");
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.itineraryCreatorManagerFragmentContainerView, currentItineraryDetailSetter, null)
                .addToBackStack(null).commit();
    }

    /**
     * Retrieve itinerary data from its fragment.
     */
    private void getItineraryFromItineraryDetailsSetter() {
        currentItinerary = currentItineraryDetailSetter.getCompiledItinerary(currentItinerary);
    }

    /**
     * It retrieves the position data (state, region, city) from the internet and passes them to the
     * fragment for setting the itinerary data.
     *
     * @param view android view
     */
    public void getGeoData(View view) {
        Log.i("UI_INTERACTION", "Premuto il pulsante \"get info from internet\".");
        LatLng coords = currentItinerary.wayPointsList.getFirst();
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addresses;
        Address address;

        try {
            addresses = geocoder.getFromLocation(coords.latitude, coords.longitude, 1);
        } catch (IOException e) {
            geocoderAddressErrorAlert();
            return;
        }
        address = addresses.get(0);
        currentItineraryDetailSetter.setGeoData(address.getCountryName(), address.getAdminArea(), address.getLocality());
    }

    /**
     * Check if a title has been set for the itinerary and, if necessary, display the error on the editText of the fragment.
     * Returns a Boolean indicating the presence of the above title.
     *
     * @return presence of itinerary title.
     */
    private boolean checkItineraryData() {
        boolean ctrl = true;
        if ((currentItinerary.title == null) || (currentItinerary.title.length() == 0)) {
            Log.i("UI_INTERACTION", "Visualizzato errore titolo assente.");
            currentItineraryDetailSetter.setTitleError(getResources().getString(R.string.fragment_itinerary_details_setter_title_length_error));
            ctrl = false;
        }
        if ((currentItinerary.state == null) || (currentItinerary.state.length() == 0)) {
            Log.i("UI_INTERACTION", "Visualizzato errore stato assente.");
            currentItineraryDetailSetter.setStateError(getResources().getString(R.string.fragment_itinerary_details_setter_state_length_error));
            ctrl = false;
        }
        if ((currentItinerary.region == null) || (currentItinerary.region.length() == 0)) {
            Log.i("UI_INTERACTION", "Visualizzato errore regione assente.");
            currentItineraryDetailSetter.setRegionError(getResources().getString(R.string.fragment_itinerary_details_setter_region_length_error));
            ctrl = false;
        }
        if ((currentItinerary.city == null) || (currentItinerary.city.length() == 0)) {
            Log.i("UI_INTERACTION", "Visualizzato errore città assente.");
            currentItineraryDetailSetter.setCityError(getResources().getString(R.string.fragment_itinerary_details_setter_city_length_error));
            ctrl = false;
        }
        return ctrl;
    }

    /**
     * Set values into internal db.
     */
    private void setInternalDb() {
        MyDatabase myDatabase = MyDatabase.getInstance(ItineraryCreatorManagerActivity.this);
        SimpleItineraryDAO simpleItineraryDAO = myDatabase.simpleItineraryDAO();

        MyDatabase.getInstance(ItineraryCreatorManagerActivity.this).simpleItineraryDAO().insertSimpleItinerary(new SimpleItinerary(
                currentItinerary.id,
                currentItinerary.title,
                currentItinerary.description,
                "my_itinerary"
        ));
    }

    /**
     * Last operation e closing this activity.
     */
    private void closeThisActivityOperations() {
        if (locationCallback != null)
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
        if (notificationManager != null)
            notificationManager.cancelAll();
        finish();
    }

    /**
     * Final procedure.
     * Retrieve the itinerary data from the fragment.
     * Call the DAO for storage.
     * Close the location manager.
     * Return to the main activity by clearing the call stack.
     *
     * @param view android view
     */
    public void finalProcedure(View view) {
        getItineraryFromItineraryDetailsSetter();
        if (!checkItineraryData())
            return;

        Log.d("CURRENT_ITINERARY", currentItinerary.toString());

        if (networkAvailable.isNetworkAvailable()) {
            showProgressDialog();
            switch (activityMode) {
                case CREATOR_MODE:
                    ItineraryDAOLambda.getInstance().saveItinerary(currentItinerary, this, new SaveItineraryResultCallback() {
                        @Override
                        public void onSuccess(int newId) {
                            progressDialog.dismiss();
                            Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                            currentItinerary.id = newId;
                            setInternalDb();
                            saveItinerarySuccessInfoAlert();
                        }

                        @Override
                        public void onFailure() {
                            progressDialog.dismiss();
                            Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                            saveItineraryFailureInfoAlert();
                        }
                    });
                    break;
                case EDITOR_MODE:
                    ItineraryDAOLambda.getInstance().updateItinerary(currentItinerary, this, new UpdateItineraryResultCallback() {
                        @Override
                        public void onSuccess() {
                            progressDialog.dismiss();
                            Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                            updateItinerarySuccessInfoAlert();
                        }

                        @Override
                        public void onFailure() {
                            progressDialog.dismiss();
                            Log.i("UI_INTERACTION", "Chiusa la process dialog.");
                            updateItineraryFailureInfoAlert();
                        }
                    });
                    break;
            }
        } else {
            Toast.makeText(this, getString(R.string.itinerary_creator_manager_network_not_available), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Initializes an empty itinerary object.
     */
    private void initializeEmptyItinerary() {
        currentItinerary = new Itinerary();
        currentItinerary.wayPointsList = new LinkedList<>();
    }

    /**
     * Initialize the editable maps fragment.
     *
     * @param pointsList list of coordinates
     */
    private void initializeEditableMap(LinkedList<LatLng> pointsList) {
        currentEditableMap = new EditableMaps(pointsList);
    }

    /**
     * Opens the map screen.
     */
    private void openEditableMap() {
        if (networkAvailable.isNetworkAvailable()) {
            Log.i("UI_INTERACTION", "Aperta schermata della mappa editabile.");
            if (currentEditableMap != null) {
                getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.itineraryCreatorManagerFragmentContainerView, currentEditableMap, null)
                        .addToBackStack(null).commit();
            }
        } else {
            Toast.makeText(this, getString(R.string.itinerary_creator_manager_network_not_available), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Procedure in case the user wants to enter the points of the itinerary manually.
     *
     * @param view android view
     */
    public void manualProcedure(View view) {
        Log.i("UI_INTERACTION", "Premuto il pulsante \"Manually\".");
        trackingMode = false;
        initializeEmptyItinerary();
        initializeEditableMap(currentItinerary.wayPointsList);
        openEditableMap();
    }

    /**
     * Procedure in case the user wants to enter the points of the itinerary via gpx file.
     *
     * @param view android view
     */
    public void gpxProcedure(View view) {
        Log.i("UI_INTERACTION", "Premuto il pulsante \"From .gpx file\".");
        trackingMode = false;
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("*/*"); //Andrebbe il tipo MIME ma non esiste per gpx
        fileBrowserLauncher.launch(intent);
        Log.i("UI_INTERACTION", "Aperta schermata del file browser.");
    }

    /**
     * Retrieve the route titles from the gpx parser.
     *
     * @return list of strings
     */
    private List<String> getRouteTitles() {
        List<Route> routeList = parsedGpx.getRoutes();
        ArrayList<String> routeTitles;

        if (routeList.size() > 0) {
            routeTitles = new ArrayList<>();
            for (int i = 0; i < routeList.size(); i++)
                routeTitles.add(routeList.get(i).getRouteName());
            return routeTitles;
        } else
            return null;
    }

    /**
     * Retrieve the list of points from the route of the gpx parser.
     *
     * @param route gpx parser route
     * @return linked list of LatLng objects
     */
    private LinkedList<LatLng> getWayPointsList(Route route) {
        List<RoutePoint> routePointList = route.getRoutePoints();
        LinkedList<LatLng> wayPointsList;

        if (routePointList.size() > 0) {
            wayPointsList = new LinkedList<>();
            for (int i = 0; i < routePointList.size(); i++) {
                wayPointsList.addLast(new LatLng(routePointList.get(i).getLatitude(),
                        routePointList.get(i).getLongitude()));
            }
            return wayPointsList;
        } else
            return null;
    }

    /**
     * Load the gpx file into the parser.
     *
     * @return successful upload state
     */
    private boolean loadGpxFile() {
        try {
            GPXParser gpxParser = new GPXParser();
            InputStream inputStream = getContentResolver().openInputStream(gpxFileUri);
            parsedGpx = gpxParser.parse(inputStream);
            Log.i("GPX_PARSER", gpxParser.toString());
            Log.i("PARSED_GPX", parsedGpx.toString());
        } catch (FileNotFoundException e1) {
            fileNotFoundExceptionAlert();
            parsedGpx = null;
            return false;
        } catch (XmlPullParserException | IOException e2) {
            xmlPullParserExceptionAlert();
            parsedGpx = null;
            return false;
        }
        return parsedGpx != null;
    }

    /**
     * Initialize the fragment to choose between the paths of the gpx file.
     *
     * @param routeTitles list of strings
     */
    private void initializeGpxItineraryChooser(List<String> routeTitles) {
        currentGpxItineraryChooser = new GpxItineraryChooser(routeTitles);
    }

    /**
     * Open the gpx route chooser screen.
     */
    private void openGpxItineraryChooser() {
        if (currentGpxItineraryChooser != null)
            getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                    .replace(R.id.itineraryCreatorManagerFragmentContainerView, currentGpxItineraryChooser, null)
                    .addToBackStack(null).commit();
        Log.i("UI_INTERACTION", "Aperta schermata scelta itinerario gpx.");
    }

    /**
     * Check if there are gpx routes to show.
     * If there are no itineraries, a warning dialog opens.
     * If there is only one itinerary, it opens the view on the map directly.
     * If there are more itineraries, it initializes and displays the selection screen.
     */
    private void gpxItineraryChooser() {
        initializeEmptyItinerary();
        if (parsedGpx.getRoutes().size() == 0)
            xmlPullParserExceptionAlert();
        else if (parsedGpx.getRoutes().size() == 1) {
            currentRoute = parsedGpx.getRoutes().get(0);
            currentItinerary.wayPointsList = getWayPointsList(currentRoute);
            initializeEditableMap(currentItinerary.wayPointsList);
            openEditableMap();
        } else {
            List<String> titleList = getRouteTitles();
            initializeGpxItineraryChooser(titleList);
            openGpxItineraryChooser();
        }
    }

    /**
     * Set the notification system.
     */
    private void setTrackingNotification() {
        //Le seguenti due righe fanno in modo da recuperare l'ultimo stato delle schermate.
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //@SuppressLint("UnspecifiedImmutableFlag") PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int)System.currentTimeMillis(), intent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), (int) System.currentTimeMillis(), intent, PendingIntent.FLAG_IMMUTABLE);

        builder = new NotificationCompat.Builder(this, NOTIFY_CHANNEL_ID)
                .setContentTitle(getResources().getString(R.string.app_name))
                .setContentText(getResources().getString(R.string.itinerary_creator_manager_recorder_notify_text))
                .setSmallIcon(R.drawable.play_icon)
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
     * Procedure in case the user wants to enter the points of the itinerary with gps tracking.
     *
     * @param view android view
     */
    public void trackingProcedure(View view) {
        Log.i("UI_INTERACTION", "Premuto il pulsante \"By GPS tracking\".");
        trackingMode = true;
        recordingStatus = false;
        setTrackingNotification();
        initializeEmptyItinerary();
        currentEditableMap = new EditableMaps(currentItinerary.wayPointsList, true);

        if (ContextCompat.checkSelfPermission(this.getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED)
            trackingPermissionErrorAlert();
        else {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            if (networkAvailable.isNetworkAvailable())
                getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                        .replace(R.id.itineraryCreatorManagerFragmentContainerView, currentEditableMap, "TRACKING_MODE")
                        .addToBackStack(null).commit();
            else
                Toast.makeText(this, getString(R.string.itinerary_creator_manager_network_not_available), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Change the trackink state in this object.
     *
     * @param view android view
     */
    public void changeTrackingState(View view) {
        Log.i("UI_INTERACTION", "Premuto il pulsante di registrazione.");
        if (trackingMode)
            recordingStatus = !recordingStatus;
        if ((trackingMode) && (recordingStatus)) {
            notificationManager.notify(NOTIFY_ID, builder.build());
            Log.i("UI_INTERACTION", "Visualizzata la notifica di tracking gps.");
        } else {
            notificationManager.cancel(NOTIFY_ID);
            Log.i("UI_INTERACTION", "Eliminata la notifica di tracking gps.");
        }
    }

    /**
     * Load the points of the chosen route into the itinerary.
     *
     * @param view android view
     */
    public void openSelectedRoute(View view) {
        Log.i("UI_INTERACTION", "Premuto il pulsante NEXT.");
        String selectedRoute = currentGpxItineraryChooser.getChoice();

        if (selectedRoute == null)
            routeSelectorErrorAlert();

        List<Route> routeList = parsedGpx.getRoutes();

        for (int i = 0; i < routeList.size(); i++) {
            if (routeList.get(i).getRouteName().equals(selectedRoute)) {
                currentRoute = routeList.get(i);
                currentItinerary.wayPointsList = getWayPointsList(routeList.get(i));
                initializeEditableMap(currentItinerary.wayPointsList);
                openEditableMap();
                break;
            }
        }
    }

    /**
     * Procedure in case the user wants to edit a itinerary.
     */
    private void editorProcedure() {
        trackingMode = false;
        recordingStatus = false;
        if (SimplyStoreManager.getInstance().containsKey("ITINERARY_TO_EDIT") && SimplyStoreManager.getInstance().getObject("ITINERARY_TO_EDIT") != null) {
            currentItinerary = (Itinerary) SimplyStoreManager.getInstance().getObjectAndDelete("ITINERARY_TO_EDIT");
            initializeEditableMap(currentItinerary.wayPointsList);
            openEditableMap();
        } else {
            currentItinerary = null;
            emptyItineraryInfoAlert();
        }
    }

    /**
     * Reset the variables to their initial state.
     */
    public void resetActivity() {
        trackingMode = false;
        recordingStatus = false;
        currentItinerary = null;
        currentEditableMap = null;
        currentItineraryDetailSetter = null;
        gpxFileUri = null;
        parsedGpx = null;
        currentRoute = null;
        currentGpxItineraryChooser = null;
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
     * Location system disabled warning.
     */
    private void locationManagerAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog richiesta attivazione localizzazione.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.fragment_editable_maps_alert_title)
                .setMessage(R.string.fragment_editable_maps_alert_location_manager_message)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                    checkAndRequestPermissions();
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante CANCEL.");
                    if (trackingMode)
                        this.onBackPressed();
                })
                .create().show();
    }

    /**
     * Waypoints list size warning.
     */
    private void wayPointsListSizeAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog punti insufficienti sulla mappa.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_alert_empty_map_message)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                })
                .create().show();
    }

    /**
     * File not found warning.
     */
    private void fileNotFoundExceptionAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog file non trovato.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_alert_file_not_found)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                })
                .create().show();
    }

    /**
     * xml/gpx parser warning.
     */
    private void xmlPullParserExceptionAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog file non non valido.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_alert_file_not_valid)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                })
                .create().show();
    }

    /**
     * Open route error warning.
     */
    private void routeSelectorErrorAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog errore apertura dell'itinerario selezionato.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_alert_route_selector_error)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                })
                .create().show();
    }

    /**
     * Geocoder information not found warning.
     */
    private void geocoderAddressErrorAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog informazioni localizzazione itinerario non trovate.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_alert_geocoder_address_error)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                })
                .create().show();
    }

    /**
     * Location permission for tracking denied.
     */
    private void trackingPermissionErrorAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog spiegazione richiesta localizzazione.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        if (android.os.Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            builder.setTitle(R.string.itinerary_creator_manager_alert_tracking_title)
                    .setMessage(R.string.itinerary_creator_manager_alert_tracking_message)
                    .setPositiveButton("Yes", (dialog, which) -> {
                        Log.i("UI_INTERACTION", "Premuto pulsante YES.");
                        checkAndRequestPermissions();
                    })
                    .setNegativeButton("Return", (dialog, which) -> {
                        Log.i("UI_INTERACTION", "Premuto pulsante RETURN.");
                    })
                    .create().show();
        } else {
            builder.setTitle(R.string.itinerary_creator_manager_alert_tracking_title)
                    .setMessage(R.string.itinerary_creator_manager_alert_tracking_message_new_system)
                    .setPositiveButton("Ok", (dialog, which) -> {
                        Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                    })
                    .create().show();
        }
    }

    /**
     * Location info alert.
     */
    private void locationServiceInfoAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog spiegazione richiesta localizzazione.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_info_location_service_title)
                .setMessage(R.string.itinerary_creator_manager_alert_info_location_service_message)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                })
                .create().show();
    }

    /**
     * Empty itinerary error alert.
     */
    private void emptyItineraryInfoAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog di itineraio vuoto.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_alert_empty_itinerary_error)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                    this.finish();
                })
                .create().show();
    }

    /**
     * Save itinerary success alert.
     */
    private void saveItinerarySuccessInfoAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog di salvataggio avvenuto con successo.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_info_title)
                .setMessage(R.string.itinerary_creator_manager_save_itinerary_success)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                    closeThisActivityOperations();
                })
                .create().show();
    }

    /**
     * Save itinerary success alert.
     */
    private void saveItineraryFailureInfoAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog di salvataggio fallito.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_save_itinerary_failure)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto il pulsante OK.");
                })
                .create().show();
    }

    /**
     * Update itinerary success alert.
     */
    private void updateItinerarySuccessInfoAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog di modifica avvenuta con successo.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_info_title)
                .setMessage(R.string.itinerary_creator_manager_update_itinerary_success)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante Ok.");
                    closeThisActivityOperations();
                })
                .create().show();
    }

    /**
     * Update itinerary success alert.
     */
    private void updateItineraryFailureInfoAlert() {
        Log.i("UI_INTERACTION", "Visualizzata dialog di modifica fallita.");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.itinerary_creator_manager_alert_warning_title)
                .setMessage(R.string.itinerary_creator_manager_update_itinerary_failure)
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante OK.");
                })
                .create().show();
    }
}
