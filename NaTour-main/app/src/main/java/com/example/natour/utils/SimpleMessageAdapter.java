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
import com.example.natour.entity.SimpleMessage;

import java.util.ArrayList;

/**
 * This classed is used to adapt different message informations into a single list item.
 * Using an ArrayList of SimpleMessage, the items will be put into a ListView.
 * @extends ArrayAdapter<SimpleMessage>
 */
public class SimpleMessageAdapter extends ArrayAdapter<SimpleMessage> {

    private Context context;
    private ArrayList<SimpleMessage> messages = new ArrayList<>();

    public SimpleMessageAdapter(Context context, ArrayList<SimpleMessage> messages){
        super(context, 0, messages);
        this.context = context;
        if(messages != null)
            this.messages.addAll(messages);
    }

    /**
     * Creates the SimpleMessage list.
     * This will set sender and message into a single list item.
     * @NonNull
     * @param position position of the list item in the view.
     * @param convertView current view.
     * @param parent parent view.
     * @return new list view.
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        SimpleMessage message = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_message, parent, false);
        }


        TextView textViewEmail =  convertView.findViewById(R.id.single_list_item_username);
        TextView textViewSurname = convertView.findViewById(R.id.single_list_item_surnameMessageFragment);
        TextView textViewName = convertView.findViewById(R.id.single_list_item_nameMessageFragment);
        TextView textViewMessage = convertView.findViewById(R.id.single_list_item_message);

        textViewEmail.setText(message.email);
        textViewSurname.setText(message.surname);
        textViewName.setText(message.name);
        textViewMessage.setText(message.message);

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
        if(this.messages == null)
            return 0;
        else
            return this.messages.size();
    }
}