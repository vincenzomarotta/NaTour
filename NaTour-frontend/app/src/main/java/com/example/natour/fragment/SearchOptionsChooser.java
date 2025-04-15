package com.example.natour.fragment;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.natour.R;
import com.example.natour.boundary.SearchActivity;
import com.example.natour.utils.UserDataPreferences;


public class SearchOptionsChooser extends DialogFragment {

    SearchActivity searchActivity;
    TextView sendMessageTextView;
    TextView promoteToAdminTextView;
    String email;
    String name;
    String surname;
    boolean isAdmin = false;


    public SearchOptionsChooser() {
        // Required empty public constructor
    }


    public SearchOptionsChooser(SearchActivity searchActivity, String email, String name, String surname) {
        this.searchActivity = searchActivity;
        this.email = email;
        this.name = name;
        this.surname = surname;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search_options_chooser, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        sendMessageTextView = view.findViewById(R.id.fragment_search_options_sendMessage);
        promoteToAdminTextView = view.findViewById(R.id.fragment_search_options_promoteToAdmin);

        UserDataPreferences userDataPreferences = new UserDataPreferences(this.searchActivity);
        isAdmin = userDataPreferences.checkUserIsAdmin();


        if(isAdmin){
            promoteToAdminTextView.setVisibility(View.VISIBLE);
            promoteToAdminTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    promoteToAdminTextView.setTextColor(getResources().getColor(R.color.red));
                    Log.i("UI_INTERACTION","Cliccato bottone di promozione ad admin all'utente.");
                    Log.i("UI_INTERACTION","Visualizza animazione di allerta per la conferma alla promozione dell'utente ad admin.");
                    new AlertDialog.Builder(searchActivity)
                            .setTitle("Attention")
                            .setMessage("Do you really want to promote this user to Admin?")
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dismiss();
                                    Log.i("UI_INTERACTION","Clicca su bottone Yes.");
                                    searchActivity.promoteToAdmin(email);
                                }})
                            .setNegativeButton(android.R.string.no, null).show();


                }
            });
        }else{
            promoteToAdminTextView.setVisibility(View.GONE);
        }

        sendMessageTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessageTextView.setTextColor(getResources().getColor(R.color.red));
                Log.i("UI_INTERACTION","Cliccato bottone di invio messaggi all'utente.");
                dismiss();
                searchActivity.openChatActivity(email,name,surname);
            }
        });

    }
}