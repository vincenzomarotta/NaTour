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
import com.example.natour.entity.SimpleMessage;
import com.example.natour.utils.SimpleMessageAdapter;

import java.util.ArrayList;

public class MessageFragment extends Fragment {

    private ListView list;
    private ArrayList<SimpleMessage> messages = new ArrayList<>();
    private TextView emptyList;

    private HomeActivity homeActivity;

    private SimpleMessageAdapter adapter;

    public MessageFragment() {

    }

    public MessageFragment(HomeActivity homeActivity) {
        this.homeActivity = homeActivity;
    }

    public MessageFragment (ArrayList<SimpleMessage> messages, HomeActivity homeActivity) {
        this.messages.addAll(messages);
        this.homeActivity = homeActivity;
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_message, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        list = view.findViewById(R.id.messages_list_view);
        emptyList = view.findViewById(R.id.empty_messages_list);
        setMessages();
    }

    /**
     * Sets the list using an array of SimpleMessage.
     * The list sets the empty view, a message showed when the list is empty.
     * Also sets the on click listener, used when the user wants to open a chat.
     */
    public void setMessages () {
        emptyList.setText(R.string.empty_list_messages);
        list.setEmptyView(emptyList);

        adapter = new SimpleMessageAdapter(getContext(), messages);
        list.setAdapter(new SimpleMessageAdapter(getContext(), messages));
        list.setOnItemClickListener((parent, view1, position, id) -> itemClicked(position));
    }

    /**
     * Method that opens the new activity of message chat.
     * @param position position of the chat in ArrayList using to get the email.
     */
    public void itemClicked(int position) {
        ((HomeActivity) requireActivity()).openMessageActivity(messages.get(position).email, messages.get(position).name, messages.get(position).surname);
    }

    @Override
    public void onResume() {
        super.onResume();
        updateList();
    }

    /**
     * Updates the chats.
     */
    public void updateList(){
        homeActivity.getChats();
    }

    /**
     * Updates the list and its adapter in the fragment.
     * @param chats chats related to the user.
     */
    public void updateList(ArrayList<SimpleMessage> chats){
        messages = new ArrayList<>(chats);
        list.setAdapter(new SimpleMessageAdapter(getContext(), messages));
    }



}