package com.example.natour.boundary;

import static java.lang.Thread.sleep;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.natour.R;
import com.example.natour.callbackinterfaces.GetNotificationNumberResultCallback;
import com.example.natour.callbackinterfaces.GetRandomItinerariesResultCallback;
import com.example.natour.callbackinterfaces.GetUserChatsResultCallback;
import com.example.natour.callbackinterfaces.GetUserListCallback;
import com.example.natour.dao.ItineraryDAOLambda;
import com.example.natour.dao.MessageDAOLambda;
import com.example.natour.utils.MyDatabase;
import com.example.natour.daointerfaces.SimpleItineraryDAO;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.entity.SimpleMessage;
import com.example.natour.fragment.FavoritesFragment;
import com.example.natour.fragment.LastSeenFragment;
import com.example.natour.fragment.MessageFragment;
import com.example.natour.fragment.MyItineraryFragment;
import com.example.natour.fragment.VisitFragment;
import com.example.natour.utils.NetworkAvailable;
import com.example.natour.utils.PagerAdapter;
import com.example.natour.utils.RememberMePreferences;
import com.example.natour.utils.UserDataPreferences;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private final String TAG = "HomeActivity";
    private static final int GET_NOTIFICATION_TIME = 10;

    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;
    private ProgressDialog progressDialog;

    private MyDatabase myDatabase;
    private SimpleItineraryDAO simpleItineraryDAO;
    private MessageDAOLambda messageDAOLambda;
    private ItineraryDAOLambda itineraryDAOLambda;

    private RememberMePreferences rememberMePreferences;
    private UserDataPreferences userDataPreferences;
    private static boolean activityRunning = true;
    private static boolean getNotificationThreadRunning = false;
    private Thread getNotificationNumber;
    private NetworkAvailable networkAvailable;

    private LastSeenFragment lastSeenFragment;
    private VisitFragment visitFragment;
    private FavoritesFragment favoritesFragment;
    private MyItineraryFragment myItineraryFragment;
    private MessageFragment messageFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        lastSeenFragment = new LastSeenFragment(this);
        myItineraryFragment = new MyItineraryFragment(this);
        visitFragment = new VisitFragment(this);
        favoritesFragment = new FavoritesFragment(this);
        messageFragment = new MessageFragment(this);

        myDatabase = MyDatabase.getInstance(HomeActivity.this);
        simpleItineraryDAO = myDatabase.simpleItineraryDAO();
        rememberMePreferences = new RememberMePreferences(HomeActivity.this);
        userDataPreferences = new UserDataPreferences(HomeActivity.this);
        setHome();
        getItinerariesList();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        activityRunning = false;
        getNotificationThreadRunning = false;
        if(!rememberMePreferences.checkRememberMe()){
            MyDatabase.getInstance(HomeActivity.this).clearAllTables();
            LoginActivity loginActivity = new LoginActivity();
            loginActivity.logout(HomeActivity.this);
        }

    }

    @Override
    protected void onResume() {
        initializeGetNotificationThread();
        super.onResume();
    }

    @Override
    protected void onPause() {
        getNotificationThreadRunning = false;
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.toolbar_home, menu);
        return true;
    }

    /**
     * Called when the activity has detected the user pressing the back button.
     */
    @Override
    public void onBackPressed() {
        Log.i("UI_INTERACTION","Premuto pulsante per tornare indietro in HomeActivity.");
        getNotificationThreadRunning = false;
        super.onBackPressed();
    }

    /**
     * Sets the DrawerLayout in the activity.
     */
    public void setHome(){
        Log.d(TAG, "Settando DrawerLayout.");
        Toolbar toolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(toolbar);
        toolbar.setTitle(R.string.welcome_message);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                this,
                drawerLayout,
                toolbar,
                R.string.openNavDraw,
                R.string.closeNavDraw
        );

        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        setTabLayout();
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }

    /**
     * Get last searched itineraries by the user if there are any.
     * If there are not itineraries, there will be shown 5 random itineraries.
     */
    public ArrayList<SimpleItinerary> getLastSeenItineraries(){
        Log.d(TAG, "Recuperando la lista degli ultimi itinerari cercati, se non ce ne sono recupero 5 itinerari random.");
        ArrayList<SimpleItinerary> lastItineraries = new ArrayList<>();
        lastItineraries = new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getLastSearched());

        if(lastItineraries.size() != 0)
            pagerAdapter.getLastSearchedFragment().updateList(lastItineraries);
        else{
            ItineraryDAOLambda.getInstance().getRandomItineraries(HomeActivity.this, new GetRandomItinerariesResultCallback() {
                @Override
                public void onSuccess(ArrayList<SimpleItinerary> randomItineraries) {
                    pagerAdapter.getLastSearchedFragment().updateList(randomItineraries);
                }

                @Override
                public void onFailure() {

                }
            });
        }

        return lastItineraries;
    }

    /**
     * Get user favorites list from local database.
     * @return ArrayList of SimpleItinerary or null.
     */
    public ArrayList<SimpleItinerary> getFavorites(){
        Log.d(TAG, "Recuperando la lista dei preferiti dell'utente.");
        return new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getFavourites());
    }

    /**
     * Get user to visit list from local database.
     * @return ArrayList of SimpleItinerary or null.
     */
    public ArrayList<SimpleItinerary> getVisit(){
        Log.d(TAG, "Recuperando la lista da visitare dell'utente.");
        return new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getToVisit());
    }

    /**
     * Get user itinerary list from local database.
     * @return ArrayList of SimpleItinerary or null.
     */
    public ArrayList<SimpleItinerary> getMyItineraries(){
        Log.d(TAG, "Recuperndo la lista degli itinerari.");
        return new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getMyItinerary());
    }

    /**
     * Get user chats trough a call to MessageDAOLambda.
     * @return ArrayList of SimpleMessage.
     */
    public ArrayList<SimpleMessage> getChats() {
        Log.d(TAG, "Recupero chat.");
        ArrayList<SimpleMessage> chats = new ArrayList<>();
        NetworkAvailable networkAvailable = new NetworkAvailable(HomeActivity.this);
        if(networkAvailable.isNetworkAvailable()) {
            MessageDAOLambda messageDAOLambda = MessageDAOLambda.getInstance();
            messageDAOLambda.getUserChats(userDataPreferences.getUserEmail(),
                    HomeActivity.this, new GetUserChatsResultCallback() {
                        @Override
                        public void onSuccess(ArrayList<SimpleMessage> userChats) {
                            Log.d(TAG, "Chat recuperate con successo.");
                            chats.addAll(userChats);
                            pagerAdapter.getMessageFragment().updateList(chats);
                        }

                        @Override
                        public void onFailure() {
                            Log.d(TAG, "Chat non recuperate.");
                        }
                    });
        }

        return chats;
    }

    /**
     * Sets the TabLayout.
     */
    public void setTabLayout(){
        Log.d(TAG, "Settando TabLayout.");
        tabLayout = findViewById(R.id.tab_layout);
        TabItem tabHome = findViewById(R.id.home_tab_item);
        TabItem tabFavorites = findViewById(R.id.list_favorites_tab_item);
        TabItem tabVisit = findViewById(R.id.list_visit_tab_item);
        TabItem tabMyList = findViewById(R.id.my_itinerary_tab_item);
        TabItem tabMessages = findViewById(R.id.messages_tab_item);
        viewPager = findViewById(R.id.view_pager);

        pagerAdapter = new PagerAdapter(getSupportFragmentManager(), tabLayout.getTabCount(), HomeActivity.this, this);

        viewPager.setAdapter(pagerAdapter);
        tabLayout.setupWithViewPager(viewPager, true);
        setIcons();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.i("UI_INTERACTION","Premuto sulla tab " + tab.getPosition() + ".");
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        initializeGetNotificationThread();
    }

    /**
     * Set icons on tabs.
     */
    public void setIcons(){
        Log.d(TAG, "Settando le icone.");
        for(int i=0; i<tabLayout.getTabCount(); i++){
            tabLayout.getTabAt(i).setIcon(pagerAdapter.setTabIcon(i));
        }
    }

    /**
     * Open Itinerary details activity
     * @param simpleItinerary of chosen itienrary
     */
    public void openItineraryDetails(SimpleItinerary simpleItinerary){
        Log.d(TAG, "Apertura schermata itinerario.");
        Log.i("UI_INTERACTION","Premuto sull'itinerario.");
        addItineraryIntoDB(simpleItinerary);
        Intent intent = new Intent(HomeActivity.this, ItineraryViewerActivity.class);
        intent.putExtra("ID", simpleItinerary.id);
        startActivity(intent);
    }

    /**
     * Opens search activity from search icon in toolbar
     * @param item item in toolbar menu
     */
    public void openSearchActivity(MenuItem item) {
        Log.d(TAG, "Apertura ricerca.");
        Log.i("UI_INTERACTION","Premuto pulsante di ricerca.");
        Intent intent = new Intent(HomeActivity.this, SearchActivity.class);
        startActivity(intent);
    }

    /**
     * Opens change password from icon in toolbar
     * @param item item in drawer menu
     */
    public void openChangePassword(MenuItem item){
        Log.d(TAG, "Apertura cambio password.");
        Log.i("UI_INTERACTION","Premuto menu item per cambio password.");
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.putExtra("changepsw", 1);
        startActivity(intent);
    }

    /**
     * Opens help from icon in toolbar
     * @param item item in drawer menu
     */
    public void openHelp(MenuItem item){
        Log.d(TAG, "Apertura info.");
        Log.i("UI_INTERACTION","Premuto menu item per info.");
        startActivity(new Intent(HomeActivity.this, IntroActivity.class));
    }

    /**
     * Uses LoginActivity method to logout the user
     * @param item item in drawer menu
     */
    public void openLogout(MenuItem item){
        Log.d(TAG, "Procedendo al logout.");
        Log.i("UI_INTERACTION","Premuto menu item per logout.");

        new AlertDialog.Builder(HomeActivity.this)
                .setTitle(getString(R.string.dialog_question))
                .setMessage(getString(R.string.about_to_logout))
                .setPositiveButton("OK", (dialog, which) -> {
                    NetworkAvailable networkAvailable = new NetworkAvailable(HomeActivity.this);
                    if(networkAvailable.isNetworkAvailable()){
                        LoginActivity loginActivity = new LoginActivity();
                        loginActivity.logout(HomeActivity.this);

                        dialog.dismiss();
                        new AlertDialog.Builder(HomeActivity.this)
                                .setTitle(getString(R.string.goodbye))
                                .setMessage(getString(R.string.see_you_soon))
                                .setPositiveButton(getString(R.string.goodbye), (dialog1, which1) ->{
                                    startActivity(new Intent(HomeActivity.this, LoginActivity.class));
                                    finish();})
                                .show();

                    } else{
                        dialog.dismiss();
                        networkAvailable.createAlertNoInternet();
                    }

                })
                .setNegativeButton(getString(R.string.back), (dialog, which) -> dialog.dismiss())
                .show();
    }

    /**
     * Opens ItineraryCreatorManagerActivity, activity used to create an itinerary.
     */
    public void openAddItinerary(){
        Log.d(TAG, "Apertura aggiunta itinerario.");
        Log.i("UI_INTERACTION","Premuto pulsante per aggiungere itinerario.");

        Intent intent = new Intent(HomeActivity.this, ItineraryCreatorManagerActivity.class);
        startActivity(intent);
    }

    /**
     * Opens MessageActivity, activity used to chat with another user.
     * @param receiver name of the message receiver
     */
    public void openMessageActivity(String receiver,String name, String surname){
        Log.d(TAG, "Apertura chat.");
        Log.i("UI_INTERACTION","Premuto su una conversazione.");
        Intent intent = new Intent(HomeActivity.this, ChatActivity.class);
        intent.putExtra("receiver",receiver);
        intent.putExtra("sender", userDataPreferences.getUserEmail());
        String fullName = name.concat(" "+surname);
        intent.putExtra("fullName",fullName);
        startActivity(intent);
    }

    /**
     * Update user itinerary list recovering data from local database.
     * @return ArrayList of SimpleItinerary.
     */
    public ArrayList<SimpleItinerary> updateMyItineraryList(){
        Log.d(TAG, "Aggiornamento lista itinerari.");
        return new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getMyItinerary());
    }

    /**
     * Update user favorite list recovering data from local database.
     * @return ArrayList of SimpleItinerary.
     */
    public ArrayList<SimpleItinerary> updateFavoriteList(){
        Log.d(TAG, "Aggiornamento lista dei preferiti.");
        return new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getFavourites());
    }

    public ArrayList<SimpleItinerary> updateLastSeen(){
        return new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getLastSearched());
    }

    /**
     * Update user to visit list recovering data from local database.
     * @return ArrayList of SimpleItinerary.
     */
    public ArrayList<SimpleItinerary> updateToVisitList(){
        Log.d(TAG, "Aggiornamento lista da visitare.");
        return new ArrayList<>(MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().getToVisit());
    }

    /**
     * Delete user last search history from local database.
     */
    public void deleteResearch(){
        Log.d(TAG, "Cancellazione ultime ricerche.");
        MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().clearLastSearched();
    }

    /**
     * Starts thread to get user message notifications and sets the number if there are any.
     */
    private void initializeGetNotificationThread(){
        Log.d(TAG, "Inizializzando thread per recupero nuovi messaggi e notifiche.");
        getNotificationThreadRunning = true;
        networkAvailable = new NetworkAvailable(HomeActivity.this);
        getNotificationNumber = new Thread(() -> {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                Log.d(TAG, "Couldn't sleep thread.");
            }

            while (getNotificationThreadRunning){
                if(networkAvailable.isNetworkAvailable()) {
                    MessageDAOLambda.getInstance().getNotificationNumber(userDataPreferences.getUserEmail(), HomeActivity.this,
                            new GetNotificationNumberResultCallback() {
                                @Override
                                public void onSuccess(int counter) {
                                    if (activityRunning) {
                                        Log.d(TAG, "Setting badge.");
                                        runOnUiThread(() -> {
                                            if (counter > 0)
                                                tabLayout.getTabAt(4).getOrCreateBadge().setNumber(counter);
                                            else
                                                tabLayout.getTabAt(4).removeBadge();
                                        });
                                    }

                                }

                                @Override
                                public void onFailure() {

                                }
                            });
                }
                try {
                    sleep(1000 * GET_NOTIFICATION_TIME);
                } catch (InterruptedException e) {
                    Log.d(TAG, "Couldn't sleep thread.");
                }
            }
        });

        getNotificationNumber.start();
    }

    /**
     * Updates user lists using ItineraryDAOLambda.
     */
    public void getItinerariesList(){
        Log.d(TAG, "Recupero liste itinerari.");
        ArrayList<SimpleItinerary> simpleItineraryArrayList = new ArrayList<>();
        networkAvailable = new NetworkAvailable(HomeActivity.this);

        if(networkAvailable.isNetworkAvailable()) {
            ItineraryDAOLambda itineraryDAOLambda = ItineraryDAOLambda.getInstance();

            itineraryDAOLambda.getUserList(new UserDataPreferences(HomeActivity.this).getUserEmail(), HomeActivity.this, new GetUserListCallback() {
                @Override
                public void onSuccess(ArrayList<SimpleItinerary> toVisitList, ArrayList<SimpleItinerary> favoriteList, ArrayList<SimpleItinerary> myItineraryList) {
                    simpleItineraryArrayList.addAll(toVisitList);
                    simpleItineraryArrayList.addAll(favoriteList);
                    simpleItineraryArrayList.addAll(myItineraryList);

                    MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().clearMyItinerary();
                    MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().clearFavorites();
                    MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().clearToVisit();

                    for (int i = 0; i < simpleItineraryArrayList.size(); i++)
                        MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().insertSimpleItinerary(simpleItineraryArrayList.get(i));

                }

                @Override
                public void onFailure() {

                }
            });
        }

    }

    private void addItineraryIntoDB(SimpleItinerary simpleItinerary){
        MyDatabase myDatabase = MyDatabase.getInstance(HomeActivity.this);
        SimpleItineraryDAO simpleItineraryDAO = myDatabase.simpleItineraryDAO();

        // simple control to avoid duplicates
        SimpleItinerary tempSimpleItinerary = MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO()
                .getItineraryByIdAndType(simpleItinerary.id, "last_searched");

        if(tempSimpleItinerary != null)
            return;

        tempSimpleItinerary = new SimpleItinerary(simpleItinerary.id, simpleItinerary.title, simpleItinerary.description, "last_searched");
        MyDatabase.getInstance(HomeActivity.this).simpleItineraryDAO().insertSimpleItinerary(tempSimpleItinerary);
    }



}