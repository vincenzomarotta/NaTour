package com.example.natour.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.example.natour.R;
import com.example.natour.boundary.SearchActivity;
import com.google.android.material.chip.Chip;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;


public class FiltersFragment extends DialogFragment {

    private SearchActivity searchActivity;
    private TextView placeTextView;
    private EditText placeEditText;
    private Button buttonApply;
    private Button buttonReset;
    private Chip chipPlace;
    private Chip chipDifficulty;
    private Chip chipDuration;
    private Chip chipAccess;
    private boolean PlaceIsChecked;
    private boolean DifficultyIsChecked;
    private boolean DurationIsChecked;
    private boolean AccessIsChecked;
    private TextView durationTextView;
    private Slider sliderDuration;
    private RatingBar ratingDifficulty;
    private TextView difficultyTextView;
    private View separating_line1;
    private View separating_line2;
    private View separating_line3;
    private View separating_line4;
    private View separating_line5;
    private Switch switchDisabled;


    public FiltersFragment() {
        // Required empty public constructor
    }


    public FiltersFragment(SearchActivity searchActivity) {
        this.searchActivity = searchActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_filters, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        placeTextView = view.findViewById(R.id.fragment_filters_place_textView);
        placeEditText = view.findViewById(R.id.fragment_filters_place_editText);
        chipPlace = view.findViewById(R.id.chipPlace);
        chipDifficulty = view.findViewById(R.id.chipDifficulty);
        chipDuration = view.findViewById(R.id.chipDuration);
        chipAccess = view.findViewById(R.id.chipAccess);
        durationTextView = view.findViewById(R.id.fragment_filters_duration_textView);
        sliderDuration = view.findViewById(R.id.fragment_filters_duration_slider);
        ratingDifficulty = view.findViewById(R.id.ratingBar_filter);
        difficultyTextView = view.findViewById(R.id.fragment_filters_difficultyTextView);
        switchDisabled = view.findViewById(R.id.accessibilitySwitch_filter);
        buttonApply = view.findViewById(R.id.fragment_filters_button_apply);
        buttonReset = view.findViewById(R.id.fragment_filters_button_reset);
        separating_line1 = view.findViewById(R.id.separating_line1);
        separating_line2 = view.findViewById(R.id.separating_line2);
        separating_line3 = view.findViewById(R.id.separating_line3);
        separating_line4 = view.findViewById(R.id.separating_line4);
        separating_line5 = view.findViewById(R.id.separating_line5);
        hideAllFilters();

        if(searchActivity.getFilters().containsKey("Place")){
            if(!(searchActivity.getFilters().get("Place").isEmpty())){
                showPlaceFilters();
                chipPlace.setChecked(true);
                setPlaceIsChecked(true);
                placeEditText.setText(searchActivity.getFilters().get("Place"));
            }
        }else{
            setPlaceIsChecked(false);
        }

        if(searchActivity.getFilters().containsKey("Duration")){
                showDurationFilters();
                chipDuration.setChecked(true);
                sliderDuration.setValue(Integer.valueOf(searchActivity.getFilters().get("Duration")));
                setDurationIsChecked(true);
        }else{
            setDurationIsChecked(false);
        }

        if(searchActivity.getFilters().containsKey("DisabledAccess")){
            showDisabledFilters();
            chipAccess.setChecked(true);
            switchDisabled.setChecked(Boolean.parseBoolean(searchActivity.getFilters().get("DisabledAccess")));
            setAccessIsChecked(true);
        }else{
            setAccessIsChecked(false);
        }

        if(searchActivity.getFilters().containsKey("Difficulty")){
            showDifficultyFilters();
            chipDifficulty.setChecked(true);
            ratingDifficulty.setRating(Integer.valueOf(searchActivity.getFilters().get("Difficulty")));
            setDifficultyIsChecked(true);
        }else{
            setDifficultyIsChecked(false);
        }

        listenToClickEvents();
        listenToItemSelectedEvents();
    }


    private void listenToClickEvents() {
        buttonApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("UI_INTERACTION","Premuto bottone applica filtri.");
                HashMap<String, String> filters = new HashMap<String, String>();
                String place = new String();
                if (isPlaceIsChecked()) {
                    place = placeEditText.getText().toString();
                    filters.put("Place", place);
                }
                if(isDifficultyIsChecked()){
                    String difficulty = Integer.toString((int) ratingDifficulty.getRating());
                    filters.put("Difficulty", difficulty);
                }
                if(isDurationIsChecked()){
                    String duration = Integer.toString((int) sliderDuration.getValue());
                    filters.put("Duration", duration);
                }
                if(isAccessIsChecked()){
                    String disabledAccess = Boolean.toString(switchDisabled.isChecked());
                    filters.put("DisabledAccess", disabledAccess);
                }

                searchActivity.setFilters(filters);
                dismiss();

            }
        });
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("UI_INTERACTION","Premuto bottone resetta filtri.");
                resetFilters();
                hideAllFilters();
            }
        });


        chipDuration.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("UI_INTERACTION","Premuto bottone resetta di attivazione filtro di Durata.");
                if(isDurationIsChecked()){
                    setDurationIsChecked(false);
                    hideDurationFilters();
                }else{
                    setDurationIsChecked(true);
                    showDurationFilters();
                }
            }
        });
        chipPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("UI_INTERACTION","Premuto bottone resetta di attivazione filtro di Posizione.");
                if(isPlaceIsChecked()){
                    setPlaceIsChecked(false);
                    hidePlaceFilters();
                }else{
                    setPlaceIsChecked(true);
                    showPlaceFilters();
                }
            }
        });
        chipDifficulty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("UI_INTERACTION","Premuto bottone resetta di attivazione filtro di Difficoltà.");
                if(isDifficultyIsChecked()){
                    setDifficultyIsChecked(false);
                    hideDifficultyFilters();
                }else{
                    setDifficultyIsChecked(true);
                    showDifficultyFilters();
                }
            }
        });
        chipAccess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("UI_INTERACTION","Premuto bottone resetta di attivazione filtro di Accessibilità per disabili.");
                if(isAccessIsChecked()){
                    setAccessIsChecked(false);
                    hideDisabledFilters();
                }else{
                    setAccessIsChecked(true);
                    showDisabledFilters();
                }
            }
        });


    }

    public void resetFilters() {
        HashMap<String, String> filters = new HashMap<String,String>();
        searchActivity.setFilters(filters);
        setDifficultyIsChecked(false);
        setAccessIsChecked(false);
        setDurationIsChecked(false);
        setPlaceIsChecked(false);
        chipPlace.setChecked(false);
        chipDifficulty.setChecked(false);
        chipDuration.setChecked(false);
        chipAccess.setChecked(false);
    }

    private void hideAllFilters() {
        placeTextView.setVisibility(View.INVISIBLE);
        placeEditText.setVisibility(View.INVISIBLE);
        durationTextView.setVisibility(View.INVISIBLE);
        sliderDuration.setVisibility(View.INVISIBLE);
        ratingDifficulty.setVisibility(View.INVISIBLE);
        difficultyTextView.setVisibility(View.INVISIBLE);
        switchDisabled.setVisibility(View.INVISIBLE);
        separating_line2.setVisibility(View.INVISIBLE);
        separating_line3.setVisibility(View.INVISIBLE);
        separating_line4.setVisibility(View.INVISIBLE);
        separating_line5.setVisibility(View.INVISIBLE);
    }

    private void showPlaceFilters() {
        placeTextView.setVisibility(View.VISIBLE);
        placeEditText.setVisibility(View.VISIBLE);
        separating_line2.setVisibility(View.VISIBLE);
    }

    private void showDurationFilters() {
        durationTextView.setVisibility(View.VISIBLE);
        sliderDuration.setVisibility(View.VISIBLE);
        separating_line3.setVisibility(View.VISIBLE);
        separating_line2.setVisibility(View.VISIBLE);
    }

    private void showDifficultyFilters() {
        ratingDifficulty.setVisibility(View.VISIBLE);
        difficultyTextView.setVisibility(View.VISIBLE);
        separating_line4.setVisibility(View.VISIBLE);
        separating_line3.setVisibility(View.VISIBLE);
    }

    private void showDisabledFilters() {
        switchDisabled.setVisibility(View.VISIBLE);
        separating_line5.setVisibility(View.VISIBLE);
        separating_line4.setVisibility(View.VISIBLE);
    }


    private void hidePlaceFilters() {
        placeTextView.setVisibility(View.INVISIBLE);
        placeEditText.setVisibility(View.INVISIBLE);
        if(!isDurationIsChecked()) {
            separating_line2.setVisibility(View.INVISIBLE);
        }
    }

    private void hideDifficultyFilters() {
        ratingDifficulty.setVisibility(View.INVISIBLE);
        difficultyTextView.setVisibility(View.INVISIBLE);
        if(!isDurationIsChecked()) {
            separating_line3.setVisibility(View.INVISIBLE);
        }
        if(!isAccessIsChecked()) {
            separating_line4.setVisibility(View.INVISIBLE);
        }
    }

    private void hideDurationFilters() {
        durationTextView.setVisibility(View.INVISIBLE);
        sliderDuration.setVisibility(View.INVISIBLE);
        if(!isDifficultyIsChecked()) {
            separating_line3.setVisibility(View.INVISIBLE);
        }
        if (!isPlaceIsChecked()){
            separating_line2.setVisibility(View.INVISIBLE);
        }
    }

    private void hideDisabledFilters() {
        switchDisabled.setVisibility(View.INVISIBLE);
        if(!isDifficultyIsChecked()){
            separating_line4.setVisibility(View.INVISIBLE);
        }
        separating_line5.setVisibility(View.INVISIBLE);
    }

    private void listenToItemSelectedEvents() {

    }


    public boolean isPlaceIsChecked() {
        return PlaceIsChecked;
    }

    public void setPlaceIsChecked(boolean placeIsChecked) {
        PlaceIsChecked = placeIsChecked;
    }


    public boolean isDifficultyIsChecked() {
        return DifficultyIsChecked;
    }

    public void setDifficultyIsChecked(boolean difficultyIsChecked) {
        DifficultyIsChecked = difficultyIsChecked;
    }


    public boolean isDurationIsChecked() {
        return DurationIsChecked;
    }

    public void setDurationIsChecked(boolean durationIsChecked) {
        DurationIsChecked = durationIsChecked;
    }



    public boolean isAccessIsChecked() {
        return AccessIsChecked;
    }

    public void setAccessIsChecked(boolean accessIsChecked) {
        AccessIsChecked = accessIsChecked;
    }


}
