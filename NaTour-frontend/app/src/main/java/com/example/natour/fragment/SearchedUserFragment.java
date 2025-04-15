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
import com.example.natour.boundary.SearchActivity;
import com.example.natour.entity.User;
import com.example.natour.utils.UserAdapter;

import java.util.ArrayList;


public class SearchedUserFragment extends Fragment {
    ListView list;
    ArrayList<User> userArrayList = new ArrayList<>();
    TextView emptyList;
    UserAdapter adapter;

    SearchActivity searchActivity;

    public SearchedUserFragment() {
        // Required empty public constructor
    }

    public SearchedUserFragment(ArrayList<User> userArrayList,SearchActivity searchActivity) {
        this.userArrayList = userArrayList;
        this.searchActivity = searchActivity;
    }


    public static SearchedUserFragment newInstance(String param1, String param2) {
        SearchedUserFragment fragment = new SearchedUserFragment();
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
        list = view.findViewById(R.id.searched_users);
        emptyList = view.findViewById(R.id.empty_searched_users_list);
        setUsers();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_searched_user, container, false);
    }

    /**
     * setLastSeenList sets the list using an array of SimpleItinerary.
     * The list also sets the empty view, a message showed when the list is empty;
     * sets the item click listener where the user can see the details of the itinerary;
     */
    public void setUsers(){
        emptyList.setText("No users found.");
        list.setEmptyView(emptyList);

        adapter = new UserAdapter(getContext(), userArrayList);
        list.setAdapter(adapter);
        list.setOnItemClickListener((parent, view1, position, id) -> itemClicked(view1, position));
    }

    /**
     * Method that opens the new activity of details viewer.
     * @param view
     * @param position
     */
    public void itemClicked(View view, int position){
        searchActivity.openSearchOptionsChooser(getContext(), userArrayList.get(position).getEmail(), userArrayList.get(position).getName(), userArrayList.get(position).getSurname());
    }

    public void updateList(){
        if(searchActivity.getSearchedUsers() == null) {
            userArrayList = null;
            list.setAdapter(new UserAdapter(getContext(), userArrayList));
        }else {
            //userArrayList.clear();
            userArrayList = (ArrayList<User>) searchActivity.getSearchedUsers().clone();
            list.setAdapter(new UserAdapter(getContext(), userArrayList));
        }
    }
}