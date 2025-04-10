package com.example.natour.fragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.example.natour.R;

import java.util.List;


public class GpxItineraryChooser extends Fragment {
    private List<String> titleList = null;
    private Spinner spinner = null;

    public GpxItineraryChooser() {
        // Required empty public constructor
    }

    public GpxItineraryChooser(List<String> titles){
        this.titleList = titles;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gpx_itinerary_chooser, container, false);
        spinner = view.findViewById(R.id.choiceSpinner_gpxItineraryChooser);
        setSpinnerValues();
        return view;
    }

    /**
     * Set the strings to insert into the spinner graphic object.
     */
    private void setSpinnerValues(){
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, titleList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    /**
     * Returns the string with the choice made by the user in the spinner.
     * @return string of choice.
     */
    public String getChoice(){
        return spinner.getSelectedItem().toString();
    }
}