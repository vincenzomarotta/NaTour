package com.example.natour.fragment;

import android.app.AlertDialog;
import android.content.Context;
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

import java.util.ArrayList;

/**
 * VisitFragment is the fragment used for "To Visit" tab.
 * Here is where to visit list is created.
 * The user can see a detailed description of each itinerary in the list
 * or take them out of the list by long pressing the list item.
 */
public class VisitFragment extends Fragment {

    private ListView list;
    private ArrayList<SimpleItinerary> itineraryArrayList = new ArrayList<>();
    private TextView emptyList;

    private HomeActivity homeActivity;
    SimpleItineraryAdapter adapter;

    public VisitFragment() {

    }

    public VisitFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    public VisitFragment(ArrayList<SimpleItinerary> itineraryArrayList, HomeActivity homeActivity) {
        this.itineraryArrayList = itineraryArrayList;
        this.homeActivity = homeActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_visit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(R.id.visit_list_view);
        emptyList = view.findViewById(R.id.empty_to_visit_list);
        setToVisitList();
    }

    /**
     * Sets the list using an array of SimpleItinerary.
     * The list also sets the empty view, a message showed when the list is empty.
     * Sets the item click listener where the user can see the details of the itinerary.
     */
    public void setToVisitList() {
        emptyList.setText(R.string.empty_list_to_visit);
        list.setEmptyView(emptyList);

        list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
        list.setOnItemClickListener((parent, view1, position, id) -> itemClicked(position));
    }

    /**
     * Method that opens the new activity of details viewer.
     * @param position position of the selected itinerary in ArrayList used to get its id.
     */
    public void itemClicked(int position){
        ((HomeActivity) requireActivity()).openItineraryDetails(itineraryArrayList.get(position));
        updateList();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    /**
     * Updates the list of itineraries.
     */
    public void updateList(){
        itineraryArrayList = new ArrayList<>(homeActivity.updateToVisitList());
        list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
    }

}