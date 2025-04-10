package com.example.natour.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.natour.R;
import com.example.natour.callbackinterfaces.ItineraryViewerFragmentReadyCallback;
import com.example.natour.callbackinterfaces.ViewMapsFragmentReadyCallback;
import com.example.natour.entity.Itinerary;
import com.example.natour.utils.GeoUtils;

public class ItineraryViewer extends Fragment {
    private boolean itineraryViewerReady = false;
    private ItineraryViewerFragmentReadyCallback itineraryViewerFragmentReadyCallback;
    private ViewMaps viewMaps;

    private TextView titleTextView;
    private TextView authorTextView;
    private ImageButton deleteItineraryButton; //FARE
    private ImageButton editItineraryButton;
    private TextView locationTextView;
    private ImageButton updateItineraryWarningButton;
    private TextView durationTextView;
    private TextView distanceTextView;
    private RatingBar difficultyRatingBar;
    private TextView disabledPeopleTextView;
    private ImageButton favouriteButton;
    private ImageButton toVisitButton;
    private ImageButton gpsSharingButton;
    private TextView descriptionTextView;

    public ItineraryViewer() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_itinerary_viewer, container, false);
        setViewerObjects(view);
        favouriteButton.setImageDrawable(getResources().getDrawable(R.drawable.empty_favorite_icon));
        DrawableCompat.setTint(toVisitButton.getDrawable(), Color.LTGRAY);
        DrawableCompat.setTint(gpsSharingButton.getDrawable(), Color.LTGRAY);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeMap();
    }

    /**
     * Set the attributes of the gui elements.
     * @param view
     */
    private void setViewerObjects(View view) {
        titleTextView = view.findViewById(R.id.titleTextView);
        authorTextView = view.findViewById(R.id.authorTextView);
        deleteItineraryButton = view.findViewById(R.id.deleteItineraryButton);
        editItineraryButton = view.findViewById(R.id.editItineraryButton);
        locationTextView = view.findViewById(R.id.locationTextView);
        updateItineraryWarningButton = view.findViewById(R.id.updateItineraryWarningButton);
        durationTextView = view.findViewById(R.id.durationTextView);
        distanceTextView = view.findViewById(R.id.distanceTextView);
        difficultyRatingBar = view.findViewById(R.id.difficultyRatingBar);
        disabledPeopleTextView = view.findViewById(R.id.disabledPeopleLabelTextView);
        favouriteButton = view.findViewById(R.id.favouriteButton);
        toVisitButton = view.findViewById(R.id.toVisitButton);
        gpsSharingButton = view.findViewById(R.id.gpsSharingButton);
        descriptionTextView = view.findViewById(R.id.descriptionTextView);
    }

    /**
     * Initializes the fragment of the map and places it in the container.
     */
    private void initializeMap(){
        viewMaps = new ViewMaps();
        viewMaps.setViewMapsFragmentReadyCallback(new ViewMapsFragmentReadyCallback() {
            @Override
            public void onViewMapsFragmentReady(ViewMaps viewMaps) {
                itineraryViewerReady = true;
                if(itineraryViewerFragmentReadyCallback != null)
                    itineraryViewerFragmentReadyCallback.onItineraryViewerFragmentReady(viewMaps);
            }
        });
        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .add(R.id.mapsFragmentContainerView, viewMaps, null)
                .addToBackStack(null).commit();
    }

    /**
     * Set the callback variable.
     * @param callback
     */
    public void setItineraryViewerFragmentReadyCallback(ItineraryViewerFragmentReadyCallback callback) {
        if(itineraryViewerReady)
            callback.onItineraryViewerFragmentReady(viewMaps);
        else
            itineraryViewerFragmentReadyCallback = callback;
    }

    /**
     * Set the route information on the screen.
     */
    public void showItineraryDataToScreen(@NonNull Itinerary itinerary) {
        titleTextView.setText(itinerary.title);
        authorTextView.setText(itinerary.ownerEmail);
        locationTextView.setText(buildLocationString(itinerary));
        if((itinerary.lastModificationUser == null) ||
                (itinerary.lastModificationUser.length() == 0) ||
                (itinerary.lastModificationUser.equals(itinerary.ownerEmail)))
            updateItineraryWarningButton.setVisibility(View.GONE);
        else
            updateItineraryWarningButton.setVisibility(View.VISIBLE);
        durationTextView.setText(new GeoUtils().convertSecondToFormattedString(itinerary.duration));
        distanceTextView.setText((String.valueOf(Math.round(itinerary.length))).concat("m."));
        difficultyRatingBar.setRating((float) itinerary.difficulty);
        if (itinerary.accessibility)
            disabledPeopleTextView.setVisibility(View.VISIBLE);
        else
            disabledPeopleTextView.setVisibility(View.INVISIBLE);
        if(itinerary.isFavourite)
            favouriteButton.setImageDrawable(getResources().getDrawable(R.drawable.fill_favorite_icon));
        if(itinerary.isToVisit)
            DrawableCompat.setTint(gpsSharingButton.getDrawable(), Color.RED);
        descriptionTextView.setText(itinerary.description);
    }

    /**
     * Returns a string formatted according to the available localization information.
     * @return formatted string.
     */
    public String buildLocationString(Itinerary itinerary) throws NullPointerException{
        if(itinerary == null)
            throw new NullPointerException();

        String location = "";

        if(itinerary.city == null && itinerary.state == null && itinerary.region == null)
            return location;

        if(itinerary.city != null && itinerary.city.length() > 0){
            location = location.concat(itinerary.city);
            if(itinerary.region == null & itinerary.state == null)
                return location;
            else{
                if((itinerary.region != null && itinerary.region.length() >0) || (itinerary.state != null && itinerary.state.length() >0))
                    location = location.concat(", ");
            }
        }


        if(itinerary.region != null && itinerary.region.length()>0) {
            location = location.concat(itinerary.region);
            if(itinerary.state == null)
                return location;
            else {
                if(itinerary.state.length() > 0)
                    location = location.concat(", ");
            }
        }

        if(itinerary.state != null && itinerary.state.length() > 0)
            location = location.concat(itinerary.state);

        return location;

/*
        if (itinerary.city != null) {
            if (itinerary.city.length() > 0) {
                location = location.concat(itinerary.city);
                if (itinerary.region != null) {
                    if (itinerary.region.length() > 0) {
                        location = location.concat(", ");
                        location = location.concat(itinerary.region);

                    }
                }  if (itinerary.state != null) {
                    if (itinerary.state.length() > 0)
                        location = location.concat(", ");
                }
            }
        }
        if (itinerary.region != null) {
            if (itinerary.region.length() > 0) {
                location = location.concat(itinerary.region);
                if (itinerary.state != null) {
                    if (itinerary.state.length() > 0)
                        location = location.concat(", ");
                }
            }
        }

        if(itinerary.state != null)
            if(itinerary.state.length() > 0)
                location = location.concat(itinerary.state);
*/

    }

    /**
     * Set the activation color of the favorite button.
     * @param value boolean value.
     */
    public void setFavoriteButtonIsActive(boolean value){
        if(value)
            favouriteButton.setImageDrawable(getResources().getDrawable(R.drawable.fill_favorite_icon));
        else
            favouriteButton.setImageDrawable(getResources().getDrawable(R.drawable.empty_favorite_icon));
    }

    /**
     * Set the activation color of the to visit button.
     * @param value boolean value.
     */
    public void setToVisitButtonIsActive(boolean value){
        if(value)
            DrawableCompat.setTint(toVisitButton.getDrawable(), Color.RED);
        else
            DrawableCompat.setTint(toVisitButton.getDrawable(), Color.LTGRAY);
    }

    /**
     * Set the visibility of the itinerary edit button.
     * @param value boolean value.
     */
    public void setEditButtonVisibility(boolean value){
        if(value)
            editItineraryButton.setVisibility(View.VISIBLE);
        else
            editItineraryButton.setVisibility(View.GONE); //Provare INVISIBLE
    }

    /**
     * Set the visibility of the delete itinerary button.
     * @param value boolean value.
     */
    public void setDeleteButtonVisibility(boolean value){
        if(value)
            deleteItineraryButton.setVisibility(View.VISIBLE);
        else
            deleteItineraryButton.setVisibility(View.GONE);
    }

    /**
     * Set the activation color of the location sharing button.
     * @param value boolean value
     */
    public void setGpsSharingButtonActiveColor(boolean value){
        if(value)
            DrawableCompat.setTint(gpsSharingButton.getDrawable(), Color.RED);
        else
            DrawableCompat.setTint(gpsSharingButton.getDrawable(), Color.LTGRAY);
    }

    /**
     * Set the ability to use the button to share the current location.
     * @param value
     */
    public void gpsShareButtonSetEnable(boolean value){
        gpsSharingButton.setEnabled(value);
    }


    /**
     * Returns a string formatted according to the available localization information.
     * @return formatted string.
     */
    public String buildLocationString(String state, String region, String city) throws NullPointerException{
        if(state == null && region == null && city == null)
            throw new NullPointerException();

        String location = "";


        if(city != null && city.length() > 0){
            location = location.concat(city);
            if(region == null & state == null)
                return location;
            else{
                if((region != null && region.length() >0) || (state != null && state.length() >0))
                    location = location.concat(", ");
            }
        }


        if(region != null && region.length()>0) {
            location = location.concat(region);
            if(state == null)
                return location;
            else {
                if(state.length() > 0)
                    location = location.concat(", ");
            }
        }

        if(state != null && state.length() > 0)
            location = location.concat(state);

        return location;

    }

}