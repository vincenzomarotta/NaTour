package com.example.natour.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.example.natour.R;
import com.example.natour.boundary.SearchActivity;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.utils.SimpleItineraryAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SearchedItineraryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SearchedItineraryFragment extends Fragment {
    ListView list;
    ArrayList<SimpleItinerary> itineraryArrayList;
    TextView emptyList;
    SimpleItineraryAdapter adapter;
    SearchActivity searchActivity;

    public SearchedItineraryFragment() {
        // Required empty public constructor
    }


    public SearchedItineraryFragment(ArrayList<SimpleItinerary> itineraryArrayList,SearchActivity searchActivity){
        this.itineraryArrayList = itineraryArrayList;
        this.searchActivity = searchActivity;
    }

    public static SearchedItineraryFragment newInstance(String param1, String param2) {
        SearchedItineraryFragment fragment = new SearchedItineraryFragment();
        Bundle args = new Bundle();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(R.id.searched_itineraries);
        emptyList = view.findViewById(R.id.empty_searched_itinerary_list);
        setItinerariesList();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_searched_itinerary, container, false);
    }

    /**
     * setLastSeenList sets the list using an array of SimpleItinerary.
     * The list also sets the empty view, a message showed when the list is empty;
     * sets the item click listener where the user can see the details of the itinerary;
     */
    public void setItinerariesList(){
        emptyList.setText("No itineraries found.");
        list.setEmptyView(emptyList);

        adapter = new SimpleItineraryAdapter(getContext(), itineraryArrayList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view1, position, id) -> itemClicked(view1, position));
    }

    /**
     * Method that opens the new activity of details viewer.
     * @param view
     * @param position
     */
    public void itemClicked(View view, int position){
        searchActivity.setInternalDb(itineraryArrayList.get(position));
        searchActivity.openItineraryDetails(getContext(), itineraryArrayList.get(position).id);
    }


    public void updateList(){
        if(searchActivity.getSearchedItineraries()==null){
            itineraryArrayList = null;
            list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
        }else {
            //this.itineraryArrayList.clear();
            itineraryArrayList = (ArrayList<SimpleItinerary>)searchActivity.getSearchedItineraries().clone();
            list.setAdapter(new SimpleItineraryAdapter(getContext(), itineraryArrayList));
        }
    }
}