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

import java.util.ArrayList;

public class FavoritesFragment extends Fragment {

    private ListView list;
    private ArrayList<SimpleItinerary> itineraryArrayList = new ArrayList<>();
    private TextView emptyList;

    private HomeActivity homeActivity;

    private SimpleItineraryAdapter adapter;

    public FavoritesFragment() {

    }

    public FavoritesFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }


    public FavoritesFragment(ArrayList<SimpleItinerary> itineraryArrayList, HomeActivity homeActivity){
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
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(R.id.favourites_list_view);
        emptyList = view.findViewById(R.id.empty_favourites_list);
        setFavoritesList();
    }

    /**
     * Sets the list using an array of SimpleItinerary.
     * The list also sets the empty view, a message showed when the list is empty.
     * Sets the item click listener where the user can see the details of the itinerary.
     */
    public void setFavoritesList() {
        emptyList.setText(R.string.empty_list_favourites);
        list.setEmptyView(emptyList);

        list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
        list.setOnItemClickListener((parent, view1, position, id) -> itemClicked(position));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    /**
     * Opens the activity of details viewer.
     * @param position position of the itinerary in the array to get its id.
     */
    public void itemClicked(int position){
        ((HomeActivity) requireActivity()).openItineraryDetails(itineraryArrayList.get(position));
    }


    /**
     * Updates the list.
     */
    public void updateList(){
        itineraryArrayList = new ArrayList<>(homeActivity.updateFavoriteList());
        list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
    }




}