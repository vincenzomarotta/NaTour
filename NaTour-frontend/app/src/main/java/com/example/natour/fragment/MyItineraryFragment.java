package com.example.natour.fragment;

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

public class MyItineraryFragment extends Fragment {

    private ListView list;
    private ArrayList<SimpleItinerary> itineraryArrayList = new ArrayList<>();
    private FloatingActionButton addItineraryFloatingButton;
    private TextView emptyList;
    private HomeActivity homeActivity;

    private SimpleItineraryAdapter adapter;

    public MyItineraryFragment() {

    }

    public MyItineraryFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    public MyItineraryFragment (ArrayList<SimpleItinerary> itineraryArrayList, HomeActivity homeActivity) {
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
        return inflater.inflate(R.layout.fragment_my_itinerary, container, false);
    }

    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(R.id.my_itinerary_list_view);
        emptyList = view.findViewById(R.id.empty_my_itineraries_list);
        addItineraryFloatingButton = view.findViewById(R.id.add_itinerary_floating_button);
        setMyItineraryList();
    }

    /**
     * Sets the list using an array of SimpleItinerary.
     * The list also sets the empty view, a message showed when the list is empty.
     * Sets the item click listener where the user can see the details of the itinerary.
     * Sets the on click listener of the floating button, used to add an itinerary.
     */
    public void setMyItineraryList() {
        emptyList.setText(R.string.empty_list_my_itinerary);
        list.setEmptyView(emptyList);

        adapter = new SimpleItineraryAdapter(getContext(), itineraryArrayList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view1, position, id) -> itemClicked(position));

        addItineraryFloatingButton.setOnClickListener(v -> floatingButtonClicked());
    }

    /**
     * Method that opens the new activity of details viewer.
     * @param position position of the selected itinerary in ArrayList used to get its id.
     */
    public void itemClicked(int position){
        ((HomeActivity) requireActivity()).openItineraryDetails(itineraryArrayList.get(position));
    }

    /**
     * Calls the opening of ItineraryCreatorManagerActivity.
     */
    public void floatingButtonClicked(){
        ((HomeActivity) requireActivity()).openAddItinerary();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    /**
     * Updates the list.
     */
    public void updateList(){
        itineraryArrayList = new ArrayList<>(homeActivity.updateMyItineraryList());
        list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
    }


}