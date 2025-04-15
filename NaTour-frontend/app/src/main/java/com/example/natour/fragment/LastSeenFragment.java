package com.example.natour.fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.natour.R;
import com.example.natour.boundary.HomeActivity;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.utils.SimpleItineraryAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


public class LastSeenFragment extends Fragment {

    private ListView list;
    private ArrayList<SimpleItinerary> itineraryArrayList = new ArrayList<>();
    private TextView emptyList;
    private FloatingActionButton deleteResearchFloatingButton;

    private SimpleItineraryAdapter adapter;
    private HomeActivity homeActivity;

    public LastSeenFragment(){

    }

    public LastSeenFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    public LastSeenFragment(ArrayList<SimpleItinerary> itineraryArrayList, HomeActivity homeActivity){
        this.itineraryArrayList = itineraryArrayList;
        this.homeActivity = homeActivity;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyList = view.findViewById(R.id.empty_searched_list);
        deleteResearchFloatingButton = view.findViewById(R.id.delete_research);
        setLastSeenList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_last_searched, container, false);
        list = view.findViewById(R.id.last_seen_itineraries);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    /**
     * Sets the list using an array of SimpleItinerary.
     * The list also sets the empty view, a message showed when the list is empty.
     * Sets the item click listener where the user can see the details of the itinerary.
     * Sets the onClickListener of the FloatingActionButton.
     */
    public void setLastSeenList(){
        emptyList.setText(R.string.empty_list_last_searched);
        list.setEmptyView(emptyList);

        adapter = new SimpleItineraryAdapter(getContext(), itineraryArrayList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view1, position, id) -> itemClicked(position));

        deleteResearchFloatingButton.setOnClickListener(v -> openAlertClearLastResearch());
    }

    /**
     * Deletes the last searched itineraries list.
     */
    public void deleteResearch(){
        ((HomeActivity) requireActivity()).deleteResearch();
        itineraryArrayList.clear();
        list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
    }

    /**
     * Method that opens the activity of details viewer.
     * @param position position of the itinerary in ArrayList in order to get its id.
     */
    public void itemClicked(int position){
        ((HomeActivity) requireActivity()).openItineraryDetails(itineraryArrayList.get(position));
    }

    /**
     * Opens AlertDialog to ask if the user is sure to delete the research history.
     */
    public void openAlertClearLastResearch(){
        new AlertDialog.Builder(getContext())
                .setTitle(getContext().getString(R.string.dialog_question))
                .setIcon(R.drawable.report_problem_icon)
                .setMessage(getString(R.string.last_searched_fragment_delete_research))
                .setPositiveButton("OK", (dialog, which) -> {
                    deleteResearch();
                })
                .setNegativeButton(getString(R.string.back), null)
                .show();
    }

    /**
     * Updates the list.
     */
    public void updateList(){
        homeActivity.getLastSeenItineraries();
    }

    /**
     * Sets the list and its adapter.
     * @param itineraries ArrayList of new itineraries.
     */
    public void updateList(ArrayList<SimpleItinerary> itineraries){
        itineraryArrayList = new ArrayList<>(itineraries);
        list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
    }

}