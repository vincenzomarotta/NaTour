package com.example.natour.boundary;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import com.example.natour.callbackinterfaces.GetSearchedItinerariesByQueryCallback;
import com.example.natour.callbackinterfaces.GetSearchedUsersByQueryCallback;
import com.example.natour.callbackinterfaces.UpdateUserAdminResultCallback;
import com.example.natour.dao.ItineraryDAOLambda;
import com.example.natour.utils.MyDatabase;
import com.example.natour.daointerfaces.SimpleItineraryDAO;
import com.example.natour.dao.UserDAOLambda;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.entity.User;
import com.example.natour.fragment.FiltersFragment;
import com.example.natour.fragment.SearchOptionsChooser;
import com.example.natour.fragment.SearchedItineraryFragment;
import com.example.natour.fragment.SearchedUserFragment;
import com.example.natour.utils.SearchPagerAdapter;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager.widget.ViewPager;


import com.example.natour.R;
import com.example.natour.utils.UserDataPreferences;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.HashMap;

public class SearchActivity extends AppCompatActivity {
    private DrawerLayout drawerLayout;
    TabLayout tabLayout;
    TabItem tabItineraries, tabUsers;
    ViewPager viewPager;
    private HashMap<String,String> filters ;
    final private String all = "ALL";
    final private String itinerariesMode = "ITINERARIES";
    final private String usersMode= "USERS";
    private ArrayList<User> searchedUsers;
    private ArrayList<SimpleItinerary> searchedItineraries;
    EditText searchEditText;
    SearchPagerAdapter pagerAdapter;
    SearchedItineraryFragment searchedItineraryFragment;
    SearchedUserFragment searchedUserFragment;
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setSearch();
    }


    public void setSearch(){
        HashMap<String,String> filters = new HashMap<String,String>();
        setFilters(filters);
        setLists();
        setUpToolbar();
        drawerLayout = findViewById(R.id.drawer_layout_searchActivity);
        setTabLayout();
    }


    private void setLists(){
        ArrayList<User> searchedUsers = new ArrayList<>();
        ArrayList<SimpleItinerary> searchedItineraries = new ArrayList<>();
        setSearchedItineraries(searchedItineraries);
        setSearchedUsers(searchedUsers);
    }

    private void setUpToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_search);
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

    @Override
    public void onBackPressed() {
        Log.i("UI_INTERACTION","Premuto pulsante onBack del cellulare, ritorna alla schermata precedente.");
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_search, menu);
        return true;
    }


    public void openFilters(MenuItem item) {
        Log.i("UI_INTERACTION","Premuto pulsante dei filtri.");
        FiltersFragment filtersFragment = new FiltersFragment(this);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager != null) {
            Log.i("UI_INTERACTION","Visualizza schermata dei filtri.");
            filtersFragment.show(fragmentManager, "FILTERS_FRAGMENT");
        }
    }

    public void startResearch(MenuItem item){
        if(isNetworkAvailable()){
            Log.i("UI_INTERACTION","Premuto pulsate di ricerca.");
            searchEditText = (EditText)  findViewById(R.id.searchActivity_editText);
            String search = searchEditText.getText().toString();

            if(search.isEmpty()){
                Snackbar.make(findViewById(R.id.drawer_layout_searchActivity), "The query is empty. Type in something and try again.", Snackbar.LENGTH_SHORT)
                        .show();
            }else {
                showProgressDialog();
                Log.i("UI_INTERACTION","Visualizza animazione di caricamento.");
                ItineraryDAOLambda iDao = ItineraryDAOLambda.getInstance();
                UserDAOLambda uDao = UserDAOLambda.getInstance();
                iDao.getSearchedItinerariesByQuery(search, this, getFilters(), new GetSearchedItinerariesByQueryCallback() {
                    @Override
                    public void onSuccess(ArrayList<SimpleItinerary> simpleItineraries) {
                            setSearchedItineraries(simpleItineraries);
                        Log.i("UI_INTERACTION","Visualizza lista di itinerari trovati.");
                            updateListItineraries();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onResultNotFound() {
                            Log.i("UI_INTERACTION","Visualizza lista vuota per risultato non trovato.");
                            setSearchedItineraries(null);
                            updateListItineraries();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure() {
                            progressDialog.dismiss();
                            showAlertDialog();
                        }
                    });

                uDao.getSearchedUsersByQuery(search, this, new GetSearchedUsersByQueryCallback() {
                         @Override
                        public void onSuccess(ArrayList<User> users) {
                             Log.i("UI_INTERACTION","Visualizza lista di utenti trovati.");
                            setSearchedUsers(users);
                            updateListUsers();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onResultNotFound() {
                            Log.i("UI_INTERACTION","Visualizza lista vuota per risultato non trovato.");
                            setSearchedUsers(null);
                            updateListUsers();
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onFailure() {
                            progressDialog.dismiss();
                            showAlertDialog();
                        }
                    });

                }
        }else{
            showAlertDialog();
        }
    }


    public void updateListUsers(){
        pagerAdapter.getSearchedUserFragment().updateList();
        setIcons();
    }

    public void updateListItineraries(){
        pagerAdapter.getSearchedItineraryFragment().updateList();
        setIcons();
    }



    public void setTabLayout(){
        tabLayout = findViewById(R.id.tab_layout_search);
        tabItineraries = findViewById(R.id.search_itinerary_tab);
        tabUsers= findViewById(R.id.search_user_tab);
        viewPager = findViewById(R.id.view_pager_Search);

        pagerAdapter = new SearchPagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), this, this);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager);
        setIcons();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i("UI_INTERACTION","Premuto su tab "+tab.getPosition()+1);
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public void setIcons(){
        for(int i=0; i<tabLayout.getTabCount(); i++){
            tabLayout.getTabAt(i).setIcon(pagerAdapter.setTabIcon(i));
        }
    }




    public void openItineraryDetails(Context context, int id){
        //Toast.makeText(context, "Vedo l'itinerario", Toast.LENGTH_SHORT).show();
        Log.i("UI_INTERACTION","Visualizza dettagli di itinerario cliccato.");
        Intent intent = new Intent(SearchActivity.this, ItineraryViewerActivity.class);
        intent.putExtra("ID", id);
        startActivity(intent);
    }

    public void openSearchOptionsChooser(Context context, String email, String name, String surname){
        Log.i("UI_INTERACTION","Visualizza opzioni di scelta per l'utente cliccato.");
        SearchOptionsChooser searchOptionsChooser = new SearchOptionsChooser(this,email, name, surname);
        FragmentManager fragmentManager = getSupportFragmentManager();
        if(fragmentManager != null) {
            searchOptionsChooser.show(fragmentManager, "SEARCH_OPTIONS_CHOOSER");
        }
    }



    public HashMap<String, String> getFilters() {
        return filters;
    }

    public void setFilters(HashMap<String, String> filters) {
        this.filters = filters;
    }



    public ArrayList<SimpleItinerary> getSearchedItineraries() {
        return searchedItineraries;
    }

    public void setSearchedItineraries(ArrayList<SimpleItinerary> searchedItineraries) {
        this.searchedItineraries = searchedItineraries;
    }

    public ArrayList<User> getSearchedUsers() {
        return searchedUsers;
    }

    public void setSearchedUsers(ArrayList<User> searchedUsers) {
        this.searchedUsers = searchedUsers;
    }

    public void promoteToAdmin(String email){
        showProgressDialog();
        Log.i("UI_INTERACTION","Visualizza animazione di caricamento.");
        UserDAOLambda uDAO = UserDAOLambda.getInstance();
        uDAO.setAdminTrue(this, email, new UpdateUserAdminResultCallback() {
            @Override
            public void onSuccess() {
                Log.i("UI_INTERACTION","Visualizza animazione di promozione dell'utente ad admin con successo.");
                progressDialog.dismiss();
                showPositivePromotionDialog();
            }

            @Override
            public void onFailure() {
                Log.i("UI_INTERACTION","Visualizza animazione di promozione dell'utente ad admin con fallimento.");
                progressDialog.dismiss();
                showAlertDialog();
            }
        });
    }

    public void openChatActivity(String email, String name, String surname){
        Log.i("UI_INTERACTION","Visualizza la chat con l'utente selezionato.");
        UserDataPreferences userDataPreferences = new UserDataPreferences(this);

        Intent intent = new Intent(this, ChatActivity.class);

        intent.putExtra("receiver",email);
        intent.putExtra("sender",userDataPreferences.getUserEmail());
        String fullName = name.concat(" "+surname);
        intent.putExtra("fullName", fullName);
        startActivity(intent);
    }

    public void setInternalDb(SimpleItinerary simpleItinerary){
        MyDatabase myDatabase = MyDatabase.getInstance(SearchActivity.this);
        SimpleItineraryDAO simpleItineraryDAO = myDatabase.simpleItineraryDAO();

        // simple control to avoid duplicates
        SimpleItinerary tempSimpleItinerary = MyDatabase.getInstance(SearchActivity.this).simpleItineraryDAO()
                .getItineraryByIdAndType(simpleItinerary.id, "last_searched");

        if(tempSimpleItinerary != null)
            return;


        simpleItinerary.listType = "last_searched";
        MyDatabase.getInstance(SearchActivity.this).simpleItineraryDAO().insertSimpleItinerary(simpleItinerary);
    }


    public void showProgressDialog(){
        progressDialog = new ProgressDialog(SearchActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);
    }

    public void showAlertDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle("WARNING")
                .setMessage("The operation was unsuccessful. Try again now or later.")
                .setIcon(getDrawable(R.drawable.warning_icon))
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION","Clicca su bottone Ok.");
                    dialog.dismiss();
                })
                .create().show();
    }

    public void showPositivePromotionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(SearchActivity.this);
        builder.setTitle("SUCCESS")
                .setMessage("The user is promoted to Admin.")
                .setIcon(getDrawable(R.drawable.success_icon))
                .setPositiveButton("Ok", (dialog, which) -> {
                    Log.i("UI_INTERACTION","Clicca su bottone Ok.");
                    dialog.dismiss();
                })
                .create().show();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


}