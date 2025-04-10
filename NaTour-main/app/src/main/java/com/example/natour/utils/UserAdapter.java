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
import com.example.natour.entity.User;

import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> {

    ArrayList<User> users = new ArrayList<>();
    Context context;

    /**
     * Creates new user adapter for a list.
     * @param context context of the application.
     * @param users list of users.
     */
    public UserAdapter(Context context, ArrayList<User> users){
        super(context, 0, users);
        this.context = context;
        if(users != null)
            this.users.addAll(users);
    }

    /**
     * Creates the SimpleItinerary list.
     * This will set itinerary title and description into a single list item.
     * @NonNull
     * @param position position in the list
     * @param convertView view of the list
     * @param parent parent where the view is located
     * @return View
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_user, parent, false);
        }

        TextView textViewEmail = convertView.findViewById(R.id.single_list_item_email);
        TextView textViewName = convertView.findViewById(R.id.single_list_item_surname);
        TextView textViewSurname = convertView.findViewById(R.id.single_list_item_name);
        TextView textViewIsAdmin = convertView.findViewById(R.id.single_list_item_isAdmin);

        textViewEmail.setText(user.getEmail());
        textViewName.setText(user.getName());
        textViewSurname.setText(user.getSurname());
        if(user.isAdmin()){
            textViewIsAdmin.setText("Admin");
        }else{
            textViewIsAdmin.setText("User");
        }

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
        if(this.users == null)
            return 0;
        else
            return this.users.size();
    }
}
