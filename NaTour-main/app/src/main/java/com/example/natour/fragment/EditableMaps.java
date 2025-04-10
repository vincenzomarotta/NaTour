package com.example.natour.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Vibrator;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.natour.R;
import com.example.natour.boundary.ItineraryCreatorManagerActivity;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.LinkedList;
import java.util.List;

public class EditableMaps extends Fragment {

    private boolean showRecordingButton = false;
    private boolean recordingStatus = false;

    private GoogleMap gMap = null;
    private FloatingActionButton recordingButton;
    private Vibrator vibe;

    private LinkedList<LatLng> latLngList;
    private LinkedList<Polyline> polylineList = null;
    private LinkedList<Marker> markerList = null;

    /**
     * This class generates a fragment containing a Google map in which a path can be drawn, that is, a preloaded path can be displayed.
     * These paths can also be modified.
     * The routes are changed on the list in real time and there is no going back with the changes.
     * In this constructor you can specify a LinkedList where the location data will be read and stored.
     * If the value of the parameter is null, the list of positions can be retrieved with the getWayPointsList method.
     *
     * @param latLngList is a LinkedList or null.
     */
    public EditableMaps(LinkedList<LatLng> latLngList) {
        this.latLngList = latLngList;
    }

    public EditableMaps(LinkedList<LatLng> latLngList, boolean showRecordingButton) {
        this.latLngList = latLngList;
        this.showRecordingButton = showRecordingButton;
    }

    private final OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            gMap = googleMap;
            setVibratorService();
            setOnMyLocationEnabled();
            setClickListener();
            setLongClickListener();
            setOnCameraMoveListener();
            setMarkerListener();
            setMarkerClickListener();
            setOnPolylineClickListener();
            if (showRecordingButton)
                setRecordingButtonClickListener();
            if (latLngList == null) latLngList = new LinkedList<>();
            if (latLngList.size() > 0) drawOnMap();
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editable_maps, container, false);
        recordingButton = view.findViewById(R.id.recordingActionButton_editableMaps);
        if (!showRecordingButton)
            recordingButton.hide();
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map_editableMaps);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    /**
     * Called when the fragment is no longer in use.  This is called
     * after {@link #onStop()} and before {@link #onDetach()}.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        ((ItineraryCreatorManagerActivity) requireActivity()).resetActivity();
    }

    /**
     * Set the click listener for the point recording function.
     */
    private void setRecordingButtonClickListener() {
        recordingButton.setOnClickListener(v -> {
            recordingStatus = !recordingStatus;
            if (recordingStatus)
                recordingButton.setImageResource(R.drawable.pause_icon);
            else
                recordingButton.setImageResource(R.drawable.play_icon);
            ((ItineraryCreatorManagerActivity) requireActivity()).changeTrackingState(getView());
            vibe.vibrate(100);
        });
    }

    /**
     * Set the smartphone vibration service.
     */
    private void setVibratorService() {
        vibe = (Vibrator) requireActivity().getSystemService(Context.VIBRATOR_SERVICE);
    }

    /**
     * Set the button for locating the current position.
     */
    public void setOnMyLocationEnabled() {
        if ((ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
            gMap.setMyLocationEnabled(true);
            gMap.getUiSettings().setMyLocationButtonEnabled(true);
        }
    }

    /**
     * Set the click listener.
     * It is used to add points on the map.
     */
    private void setClickListener(){
        /*gMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                vibe.vibrate(100);
                addWayPoint(latLng);
                Log.i("UI_INTERACTION", "Aggiunto waypoint.");
            }
        });*/

    }

    /**
     * Set the long click listener.
     * It is used to add points on the map.
     */
    private void setLongClickListener() {
        gMap.setOnMapLongClickListener(latLng -> {
            vibe.vibrate(100);
            addWayPoint(latLng);
            Log.i("UI_INTERACTION", "Aggiunto waypoint.");
        });
    }

    /**
     * Set the camera move listener.
     */
    private void setOnCameraMoveListener() {
        gMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {
                Log.i("UI_INTERACTION", "Movimento mappa.");
            }
        });
    }

    /**
     * Set the marker listener.
     * It is used to change the position of points on the map.
     */
    private void setMarkerListener() {
        gMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDrag(@NonNull Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(@NonNull Marker marker) {
                Log.i("UI_INTERACTION", "Marker waypoint spostato.");
                modifyWayPointPosition(marker);
                refreshMap();
            }

            @Override
            public void onMarkerDragStart(@NonNull Marker marker) {
                vibe.vibrate(200);
            }
        });
    }

    /**
     * Set the polyline click listener.
     * It is used to add a midpoint on a line.
     */
    private void setOnPolylineClickListener() {
        gMap.setOnPolylineClickListener(polyline -> {
            Log.i("UI_INTERACTION", "Aggiunto marker su polyline.");
            vibe.vibrate(300);
            addWayPointOnPolyline(polyline);
        });
    }

    /**
     * Set the marker click listener.
     * It is used to display the marker edit menu.
     */
    private void setMarkerClickListener() {
        gMap.setOnMarkerClickListener(marker -> {
            Log.i("UI_INTERACTION", "Premuto su marker.");
            vibe.vibrate(500);
            //removeWayPoint(marker);
            confirmAndRemoveWayPoint(marker);
            return true;
        });
    }

    /**
     * Add a point on the list and refresh the map.
     *
     * @param newCoords new coordinates passed by listener.
     */
    public void addWayPoint(@NonNull LatLng newCoords) {
        if (latLngList == null)
            latLngList = new LinkedList<>();
        latLngList.addLast(newCoords);
        refreshMap();
    }

    /**
     * Add a point on the list, refresh the map and set the view and zoom to new coords.
     *
     * @param newCoords new coordinates passed by listener.
     */
    public void addWayPointAndMoveView(@NonNull LatLng newCoords) {
        if (latLngList == null)
            latLngList = new LinkedList<>();
        latLngList.addLast(newCoords);
        refreshMap();

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(newCoords)
                .zoom(15)
                .build();
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cameraPosition);
        gMap.animateCamera(cu);
    }

    /**
     * Delete a point on the list and refresh the map.
     *
     * @param marker reference to the marker passed by the listener.
     */
    private void removeWayPoint(@NonNull Marker marker) {
        if (marker.getTag() != null) {
            int markerIndexTag = (Integer) marker.getTag();

            latLngList.remove(markerIndexTag);
            refreshMap();
        }
    }

    /**
     * Change the position of a point on the list and refresh the map.
     *
     * @param marker reference to the marker passed by the listener.
     */
    private void modifyWayPointPosition(@NonNull Marker marker) {
        if (marker.getTag() != null) {
            int markerIndexTag = (Integer) marker.getTag();

            LatLng newcoords = marker.getPosition();
            if ((markerIndexTag < 0) || (markerIndexTag >= markerList.size())) {
                return;
            }
            latLngList.set(markerIndexTag, newcoords);
            refreshMap();
        }
    }

    /**
     * Adds the midpoint of a line to the list and refreshes the map.
     *
     * @param polyline polyline reference passed by listener.
     */
    private void addWayPointOnPolyline(@NonNull Polyline polyline) {
        int polylineIndex = 0;
        List<LatLng> segmentPoints = null;

        for (int i = 0; i < polylineList.size(); i++) {
            if (polylineList.get(i).equals(polyline)) {
                polylineIndex = i;
                segmentPoints = polylineList.get(i).getPoints();
                break;
            }
        }
        if (segmentPoints != null) {
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(segmentPoints.get(0)).include(segmentPoints.get(1));
            latLngList.add((polylineIndex + 1), builder.build().getCenter());
            refreshMap();
        }
    }

    /**
     * Draw the lines on the map.
     * Create the list of lines and draw them on the map.
     * In case there are no points, that is, there is only one point, the method returns without doing anything.
     */
    private void drawPolylines() {
        if (latLngList.size() < 2)
            return;

        polylineList = new LinkedList<>();
        for (int i = 1; i < latLngList.size(); i++) {
            polylineList.add(gMap.addPolyline(new PolylineOptions().add(latLngList.get(i - 1),
                    latLngList.get(i)).color(Color.GREEN).width(5).clickable(true)));
        }
    }

    /**
     * Draw the markers on the map.
     * Create the marker list and draw them on the map.
     */
    private void drawMarkers() {
        markerList = new LinkedList<>();
        for (int i = 0; i < latLngList.size(); i++) {
            markerList.add(gMap.addMarker(new MarkerOptions().position(latLngList.get(i)).draggable(true)));
            markerList.get(i).setTag(i);
            markerList.get(i).setTitle(Integer.toString(i));
            markerList.get(i).showInfoWindow();
            if (i == 0)
                markerList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            if (i == (latLngList.size() - 1))
                markerList.get(i).setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
        }
    }

    /**
     * Set the location and zoom level for a route and bring the map to the viewing area.
     */
    private void setZoom() {
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (int i = 0; i < latLngList.size(); i++)
            builder.include(latLngList.get(i));
        LatLngBounds bounds = builder.build();

        DisplayMetrics displayMetrics = new DisplayMetrics();
        Display display = requireActivity().getWindowManager().getDefaultDisplay();
        display.getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;
        int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
        gMap.animateCamera(cameraUpdate);
    }

    /**
     * Map refresh.
     * He first cleans the map, then draws the lines and finally draws the marker.
     */
    public void refreshMap() {
        gMap.clear();
        drawPolylines();
        drawMarkers();
    }

    /**
     * Draw on the map.
     * He first draws the lines, then draws the markers and finally set zoom.
     */
    private void drawOnMap() {
        drawPolylines();
        drawMarkers();
        setZoom();
    }

    /**
     * Retrieve the list of points drawn on the map.
     * If the map does not contain points, it returns null, otherwise it returns a LinkedList containing the points.
     *
     * @return LinkedList or null.
     */
    public LinkedList<LatLng> getWayPointsList() {
        if (this.latLngList.size() == 0)
            return null;
        else
            return this.latLngList;
    }

    /**
     * It asks for confirmation for the deletion of the point.
     *
     * @param marker marker
     */
    private void confirmAndRemoveWayPoint(@NonNull Marker marker) {
        Log.i("UI_INTERACTION", "Visualizzata dialog eliminazione waypoint.");
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(R.string.fragment_editable_maps_alert_delete)
                .setMessage(R.string.fragment_editable_maps_alert_delete_point)
                .setPositiveButton("Confirm", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante CONFIRM.");
                    Log.i("UI_INTERACTION", "Waypoint cancellato.");
                    removeWayPoint(marker);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    Log.i("UI_INTERACTION", "Premuto pulsante CANCEL.");
                })
                .create().show();
    }

    /**
     * Set recording status flag.
     *
     * @param recordingStatus status
     */
    public void setRecordingStatus(boolean recordingStatus) {
        this.recordingStatus = recordingStatus;
    }
}