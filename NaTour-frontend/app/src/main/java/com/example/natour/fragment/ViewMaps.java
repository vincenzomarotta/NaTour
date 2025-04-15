package com.example.natour.fragment;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.natour.R;
import com.example.natour.callbackinterfaces.ViewMapsFragmentReadyCallback;
import com.example.natour.entity.SharedPosition;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public class ViewMaps extends Fragment {
    private boolean mapReady = false;
    private ViewMapsFragmentReadyCallback viewMapsFragmentReadyCallback;

    private GoogleMap gMap;
    private List<LatLng> latLngList;
    private List<Marker> currentPositionMarkerList;

    private OnMapReadyCallback callback = new OnMapReadyCallback() {
        @Override
        public void onMapReady(@NonNull GoogleMap googleMap) {
            gMap = googleMap;
            gMap.getUiSettings().setMapToolbarEnabled(false);
            setScrollViewerDisabling();
            mapReady = true;
            if(viewMapsFragmentReadyCallback != null)
                viewMapsFragmentReadyCallback.onViewMapsFragmentReady(ViewMaps.this);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_view_maps, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(callback);
        }
    }

    /**
     * Disable the intercept touch event on scrollview when an user move the map.
     */
    private void setScrollViewerDisabling() {
        gMap.setOnCameraMoveStartedListener(i -> requireView().getParent().requestDisallowInterceptTouchEvent(true));
        gMap.setOnCameraIdleListener(() -> requireView().getParent().requestDisallowInterceptTouchEvent(false));
    }

    /**
     * Draw the polyline on map from a LatLng list passed on constructor.
     */
    private void drawPolyline() {
        if (latLngList != null) {
            if (latLngList.size() >= 2) {
                gMap.addPolyline(new PolylineOptions()
                        .clickable(false)
                        .color(Color.GREEN)
                        .addAll(latLngList));
            }
        }
    }

    /**
     * Draw the markers to start and end of route.
     */
    private void drawStartFinishMarker(){
        if((latLngList != null) && (latLngList.size() > 0)){
            if(latLngList.size() == 1){
                gMap.addMarker(new MarkerOptions()
                .position(latLngList.get(0))
                .visible(true)
                .draggable(false)
                .title(getString(R.string.fragment_view_maps_single_position_title))
                .flat(true)
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            }
            else {
                gMap.addMarker(new MarkerOptions()
                        .position(latLngList.get(0))
                        .visible(true)
                        .draggable(false)
                        .title(getString(R.string.fragment_view_maps_start_position_title))
                        .flat(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN)));
                gMap.addMarker(new MarkerOptions()
                        .position(latLngList.get(latLngList.size() - 1))
                        .visible(true)
                        .draggable(false)
                        .title(getString(R.string.fragment_view_maps_finish_position_title))
                        .flat(true)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
            }
        }
    }

    /**
     * Set the map zoom considering the LatLng list.
     */
    private void setZoom() {
        if(latLngList != null){
            LatLngBounds.Builder builder = new LatLngBounds.Builder();
            for (int i = 0; i < latLngList.size(); i++)
                builder.include(latLngList.get(i));
            LatLngBounds bounds = builder.build();

            DisplayMetrics displayMetrics = new DisplayMetrics();
            Display display = requireActivity().getWindowManager().getDefaultDisplay();
            display.getMetrics(displayMetrics);

            int width = requireView().getWidth();
            int height = requireView().getHeight();
            int padding = (int) (width * 0.12); // offset from edges of the map 12% of screen

            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, width, height, padding);
            gMap.animateCamera(cameraUpdate);
        }
    }

    /**
     * Returns the reference to this object.
     * @return ViewMaps object
     */
    public ViewMaps getViewMapsObjectInstance() {
        return this;
    }

    /**
     * Set and display on the map the current location of users sharing their location.
     * @param sharedPositionList List of shared locations
     */
    public void setCurrentPositionMarkerPosition(List<SharedPosition> sharedPositionList) {
        if ((sharedPositionList != null) && (gMap != null)) {
            currentPositionMarkerList = new LinkedList<>();
            gMap.clear();
            drawPolyline();
            drawStartFinishMarker();
            for (int i = 0; i < sharedPositionList.size(); i++) {
                SharedPosition tempShared = sharedPositionList.get(i);
                LatLng tempPosition = new LatLng(tempShared.latitude, tempShared.longitude);
                Marker tempMarker = gMap.addMarker(new MarkerOptions().position(tempPosition).visible(true));
                Objects.requireNonNull(tempMarker).setTitle(tempShared.user);
                currentPositionMarkerList.add(tempMarker);
            }
        }
    }

    /**
     * Notify when the object is ready.
     * @param callback
     */
    public void setViewMapsFragmentReadyCallback(ViewMapsFragmentReadyCallback callback) {
        if(mapReady)
            callback.onViewMapsFragmentReady(this);
        else
            this.viewMapsFragmentReadyCallback = callback;
    }

    /**
     * View the itinerary on the map.
     * @param list
     */
    public void showRoute(List<LatLng> list){
        latLngList = list;
        drawPolyline();
        drawStartFinishMarker();
        setZoom();
    }
}