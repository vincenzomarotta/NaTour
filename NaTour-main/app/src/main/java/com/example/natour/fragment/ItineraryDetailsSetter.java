package com.example.natour.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.natour.R;
import com.example.natour.entity.Itinerary;
import com.google.android.material.textfield.TextInputLayout;

public class ItineraryDetailsSetter extends Fragment {
    private EditText titleEditText = null;
    private EditText stateEditText = null;
    private EditText regionEditText = null;
    private EditText cityEditText = null;
    private Switch accessibilitySwitch = null;
    private Switch isPrivateEditSwitch = null;
    private TextView durationTextView = null;
    private TextView lengthTextView = null;
    private RatingBar difficultyRaringBar = null;
    private EditText descriptionEditText = null;

    private TextInputLayout titleTextInputLayout = null;
    private TextInputLayout stateTextInputLayout = null;
    private TextInputLayout regionTextInputLayout = null;
    private TextInputLayout cityTextInputLayout = null;
    private TextInputLayout descriptionTextInputLayout = null;

    private String title;
    private String state;
    private String region;
    private String city;
    private boolean accessibility;
    private boolean isPrivate;
    private String duration;
    private String length;
    private float difficulty;
    private String description;

    public ItineraryDetailsSetter() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_itinerary_details_setter, container, false);

        titleTextInputLayout = view.findViewById(R.id.titleEditText_itineraryDetailsSetter);
        titleEditText = titleTextInputLayout.getEditText();
        stateTextInputLayout = view.findViewById(R.id.stateEditText_itineraryDetailsSetter);
        stateEditText = stateTextInputLayout.getEditText();
        regionTextInputLayout = view.findViewById(R.id.regionEditText_itineraryDetailsSetter);
        regionEditText = regionTextInputLayout.getEditText();
        cityTextInputLayout = view.findViewById(R.id.cityEditText_itineraryDetailsSetter);
        cityEditText = cityTextInputLayout.getEditText();
        accessibilitySwitch = view.findViewById(R.id.accessibilitySwitch_itineraryDetailsSetter);
        isPrivateEditSwitch = view.findViewById(R.id.isPrivateSwitch_itineraryDetailsSetter);
        durationTextView = view.findViewById(R.id.estimatedTime_itineraryDetailsSetter);
        lengthTextView = view.findViewById(R.id.effectiveRouteLength_itineraryDetailsSetter);
        difficultyRaringBar = view.findViewById(R.id.difficultyRatingBar_itineraryDetailsSetter);
        descriptionTextInputLayout = view.findViewById(R.id.descriptionTextMultiLine_itineraryDetailsSetter);
        descriptionEditText = descriptionTextInputLayout.getEditText();

        showAvailableData();
        setOnListenerEditText();

        return view;
    }

    /**
     * Set the information passed as arguments.
     */
    private void setArguments(){
        if(getArguments().containsKey("TITLE"))
            title = getArguments().getString("TITLE");
        if(getArguments().containsKey("STATE"))
            state = getArguments().getString("STATE");
        if(getArguments().containsKey("REGION"))
            region = getArguments().getString("REGION");
        if(getArguments().containsKey("CITY"))
            city = getArguments().getString("CITY");
        if(getArguments().containsKey("ACCESSIBILITY"))
            accessibility = getArguments().getBoolean("ACCESSIBILITY");
        if(getArguments().containsKey("IS_PRIVATE"))
            isPrivate = getArguments().getBoolean("IS_PRIVATE");
        if(getArguments().containsKey("DURATION"))
            duration = getArguments().getString("DURATION");
        if(getArguments().containsKey("LENGTH"))
            length = getArguments().getString("LENGTH");
        if(getArguments().containsKey("DIFFICULTY"))
            difficulty = getArguments().getFloat("DIFFICULTY");
        if(getArguments().containsKey("DESCRIPTION"))
            description = getArguments().getString("DESCRIPTION");
    }

    /**
     * Set the information on the screen.
     */
    private void showAvailableData(){
        if(getArguments().containsKey("TITLE"))
            titleEditText.setText(title);
        if(getArguments().containsKey("STATE"))
            stateEditText.setText(state);
        if(getArguments().containsKey("REGION"))
            regionEditText.setText(region);
        if(getArguments().containsKey("CITY"))
            cityEditText.setText(city);
        if(getArguments().containsKey("ACCESSIBILITY"))
            accessibilitySwitch.setChecked(accessibility);
        if(getArguments().containsKey("IS_PRIVATE"))
            isPrivateEditSwitch.setChecked(isPrivate);
        if(getArguments().containsKey("TIME_TRAVEL"))
            durationTextView.setText(duration);
        if(getArguments().containsKey("ROUTE_LENGTH"))
            lengthTextView.setText(length);
        if(getArguments().containsKey("DIFFICULTY"))
            difficultyRaringBar.setRating(difficulty);
        if(getArguments().containsKey("DESCRIPTION"))
            descriptionEditText.setText(description);
    }

    /**
     * Displays information on localizations retrieved from the internet on the screen.
     * @param state
     * @param region
     * @param city
     */
    public void setGeoData(String state, String region, String city){
        this.stateTextInputLayout = getActivity().findViewById(R.id.stateEditText_itineraryDetailsSetter);
        this.regionTextInputLayout = getActivity().findViewById(R.id.regionEditText_itineraryDetailsSetter);
        this.cityTextInputLayout = getActivity().findViewById(R.id.cityEditText_itineraryDetailsSetter);

        this.stateTextInputLayout.getEditText().setText(state);
        this.regionTextInputLayout.getEditText().setText(region);
        this.cityTextInputLayout.getEditText().setText(city);
    }

    /**
     * Return the complete itinerary with all the information entered.
     * @param itinerary
     * @return
     */
    public Itinerary getCompiledItinerary(Itinerary itinerary){
        if(itinerary == null)
            itinerary = new Itinerary();

        itinerary.title = titleEditText.getText().toString();
        itinerary.state = stateEditText.getText().toString();
        itinerary.region = regionEditText.getText().toString();
        itinerary.city = cityEditText.getText().toString();
        itinerary.accessibility = accessibilitySwitch.isChecked();
        itinerary.isPrivate = isPrivateEditSwitch.isChecked();
        itinerary.difficulty = (int) difficultyRaringBar.getRating();
        itinerary.description = descriptionEditText.getText().toString();

        return itinerary;
    }

    /**
     * Setting up the text field listeners.
     */
    public void setOnListenerEditText(){
        titleEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                titleTextInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        stateEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                stateTextInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        regionEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                regionTextInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        cityEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cityTextInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Set the error on the title text field.
     * @param errorMessage
     */
    public void setTitleError(String errorMessage){
        titleTextInputLayout.setError(errorMessage);
    }

    /**
     * Set the error on the state text field.
     * @param errorMessage
     */
    public void setStateError(String errorMessage){
        stateTextInputLayout.setError(errorMessage);
    }

    /**
     * Set the error on the region text field.
     * @param errorMessage
     */
    public void setRegionError(String errorMessage){ regionTextInputLayout.setError(errorMessage); }

    /**
     * Set the error on the city text field.
     * @param errorMessage
     */
    public void setCityError(String errorMessage){
        cityTextInputLayout.setError(errorMessage);
    }

    /**
     * Set the error on the description text field.
     * @param errorMessage
     */
    public void setDescriptionError(String errorMessage){
        descriptionEditText.setError(errorMessage);
    }
}