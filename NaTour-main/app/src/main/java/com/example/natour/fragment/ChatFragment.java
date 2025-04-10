package com.example.natour.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.example.natour.R;
import com.example.natour.boundary.ChatActivity;
import com.example.natour.utils.MessageListAdapter;

public class ChatFragment extends Fragment {
    private ChatActivity chatActivity;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText mEditMessage;
    private TextView toolbarTextView;

    public ChatFragment(ChatActivity chatActivity) {
        this.chatActivity = chatActivity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mEditMessage = view.findViewById(R.id.edit_message);
        mMessageRecycler = view.findViewById(R.id.recycler_chat);
        toolbarTextView = view.findViewById(R.id.toolbarTextViewChat);
    }
}