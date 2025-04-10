package com.example.natour.boundary;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.natour.R;
import com.example.natour.callbackinterfaces.GetMessagesResultCallback;
import com.example.natour.callbackinterfaces.SaveMessageResultCallback;
import com.example.natour.dao.MessageDAOLambda;
import com.example.natour.entity.Message;
import com.example.natour.fragment.ChatFragment;
import com.example.natour.utils.MessageListAdapter;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class ChatActivity extends AppCompatActivity {
    private ChatFragment chatFragment;
    private RecyclerView mMessageRecycler;
    private MessageListAdapter mMessageAdapter;
    private EditText mEditMessage;
    private String sender;
    private String receiver;
    private String fullName;
    private ArrayList<Message> messageList;
    private TextView toolbarTextView;
    ProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        Intent i = getIntent();
        this.receiver = i.getStringExtra("receiver");
        this.sender = i.getStringExtra("sender");
        this.fullName = i.getStringExtra("fullName");

        toolbarTextView = (TextView) findViewById(R.id.toolbarTextViewChat);
        toolbarTextView.setText(fullName);
        setUpToolbar();
        getAllMessages();

    }

    public void sendMessage(View view){
        Log.i("UI_INTERACTION","Cliccato su bottone per l'invio di messaggi.");
        mEditMessage = (EditText) findViewById(R.id.edit_message);
        if(!(mEditMessage.getText().toString().isEmpty())) {
            Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());

            Message newMessage = new Message(sender, receiver, mEditMessage.getText().toString(), false, currentTime);
            mEditMessage.setText("");
            MessageDAOLambda mDAO = MessageDAOLambda.getInstance();
            mDAO.saveMessage(newMessage, this, new SaveMessageResultCallback(){
                @Override
                public void onSuccess() {
                    if (!(getMessageList() == null)) {
                        getMessageList().add(newMessage);
                    }else{
                        messageList = new ArrayList<>();
                        messageList.add(newMessage);
                        setMessageList(messageList);
                        mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_chat);
                    }

                    mMessageAdapter = new MessageListAdapter(ChatActivity.this, getMessageList(), sender);
                    mMessageRecycler.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                    mMessageRecycler.setAdapter(mMessageAdapter);
                    mMessageRecycler.smoothScrollToPosition(getMessageList().size());

                }

                @Override
                public void onFailure() {
                    showAlertDialog();
                }
            });

            if (!(getMessageList() == null)) {
                getMessageList().add(newMessage);
            }else{
                messageList = new ArrayList<>();
                messageList.add(newMessage);
                setMessageList(messageList);
                mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_chat);
            }

            mMessageAdapter = new MessageListAdapter(this, getMessageList(), sender);
            mMessageRecycler.setLayoutManager(new LinearLayoutManager(this));
            mMessageRecycler.setAdapter(mMessageAdapter);
            mMessageRecycler.smoothScrollToPosition(getMessageList().size());
        }

    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    public ArrayList<Message> getMessageList() {
        return messageList;
    }

    public void setMessageList(ArrayList<Message> messageList) {
        this.messageList = messageList;
    }


    public void showProgressDialog(){
        progressDialog = new ProgressDialog(ChatActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
    }

    @Override
    public void onBackPressed() {
        Log.i("UI_INTERACTION","Premuto pulsante onBack del cellulare, ritorna alla schermata precedente.");
        finish();
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_chat);
        toolbar.setTitle("");
        if (this != null) {
            this.setSupportActionBar(toolbar);
        }
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("UI_INTERACTION","Premuto pulsante indietro nella Toolbar, ritorna alla schermata precedente.");
                finish();
            }
        });
    }

    private void getAllMessages(){
        Log.i("UI_INTERACTION","Visualizza la chat con l'utente selezionato.");
        if(isNetworkAvailable()) {
            ArrayList<Message> messageList = new ArrayList<Message>();
            MessageDAOLambda mDAO = MessageDAOLambda.getInstance();
            Log.i("UI_INTERACTION","Visualizza l'animazione di caricamento della chat di messaggi.");
            showProgressDialog();
            mDAO.getMessages(sender,receiver,this, new GetMessagesResultCallback(){
                @Override
                public void onSuccess(ArrayList<Message> messages) {
                    Log.i("UI_INTERACTION","Visualizza tutti i messaggi scambiati con l'utente selezionato.");
                    setMessageList(messages);
                    mMessageRecycler = (RecyclerView) findViewById(R.id.recycler_chat);
                    mMessageAdapter = new MessageListAdapter(ChatActivity.this, /*getMessageList()*/messages, sender);
                    mMessageRecycler.setLayoutManager(new LinearLayoutManager(ChatActivity.this));
                    mMessageRecycler.setAdapter(mMessageAdapter);
                    mMessageRecycler.smoothScrollToPosition(/*getMessageList()*/messages.size());
                    progressDialog.dismiss();
                }

                @Override
                public void onFailure() {
                    showAlertDialog();
                }

                @Override
                public void onResultNotFound(){
                    progressDialog.dismiss();
                }
            });
        }else{
            showAlertDialog();
        }
    }

    public void showAlertDialog(){
        new AlertDialog.Builder(ChatActivity.this)
                .setTitle(getString(R.string.try_again))
                .setMessage(R.string.smtng_wrong)
                .setIcon(getDrawable(R.drawable.error_icon))
                .setPositiveButton("OK", (dialog, which) -> {
                    dialog.dismiss();
                    finish();
                })
                .show();
    }
}
