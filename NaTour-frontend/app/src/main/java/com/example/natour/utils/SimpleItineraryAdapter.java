package com.example.natour.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.natour.R;
import com.example.natour.entity.SimpleItinerary;

import java.util.ArrayList;

/**
 * This class is used to adapt different itinerary information into a single list item.
 * Using an ArrayList of SimpleItinerary, the items will be put into a ListView.
 * @extends ArrayAdapter<SimpleItinerary>
 */
public class SimpleItineraryAdapter extends ArrayAdapter<SimpleItinerary> {

    ArrayList<SimpleItinerary> itineraries = new ArrayList<>();
    Context context;

    public SimpleItineraryAdapter(Context context, ArrayList<SimpleItinerary> itineraries){
        super(context, 0, itineraries);
        this.context = context;
        if(itineraries != null)
            this.itineraries.addAll(itineraries);
    }

    /**
     * Creates the SimpleItinerary list for ListView.
     * This will set itinerary title and description into a single list item.
     * @NonNull
     * @param position of the list item in the view.
     * @param convertView current view.
     * @param parent parent view.
     * @return new list view.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SimpleItinerary itinerary = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_itinerary, parent, false);
        }

        TextView textViewTitle = convertView.findViewById(R.id.single_list_item_title);
        TextView textViewDescription = convertView.findViewById(R.id.single_list_item_description);

        textViewTitle.setText(itinerary.title);
        textViewDescription.setText(itinerary.description);

        return convertView;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }

    /**
     * Get the counts of the items into a list.
     * If the list is null, the count is 0
     * @return items number
     */
    @Override
    public int getCount(){
        if(this.itineraries == null)
            return 0;
        else
            return this.itineraries.size();
    }
}
