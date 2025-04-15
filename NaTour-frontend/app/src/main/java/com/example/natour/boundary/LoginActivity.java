package com.example.natour.boundary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoDevice;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserCodeDeliveryDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.AuthenticationDetails;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ChallengeContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.ForgotPasswordContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.continuations.MultiFactorAuthenticationContinuation;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.AuthenticationHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.ForgotPasswordHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.example.natour.R;
import com.example.natour.callbackinterfaces.GetUserListCallback;
import com.example.natour.callbackinterfaces.GetUserResultCallback;
import com.example.natour.callbackinterfaces.LoginResultCallback;
import com.example.natour.dao.ItineraryDAOLambda;
import com.example.natour.utils.MyDatabase;
import com.example.natour.daointerfaces.SimpleItineraryDAO;
import com.example.natour.dao.UserDAOLambda;
import com.example.natour.entity.SimpleItinerary;
import com.example.natour.entity.User;
import com.example.natour.exceptions.AuthException;
import com.example.natour.fragment.ChangePasswordFragment;
import com.example.natour.fragment.ForgotPasswordCodeFragment;
import com.example.natour.fragment.ForgotPasswordEmailFragment;
import com.example.natour.fragment.LoginFragment;
import com.example.natour.utils.CognitoSettings;
import com.example.natour.utils.NetworkAvailable;
import com.example.natour.utils.RememberMePreferences;
import com.example.natour.utils.UserDataPreferences;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {

    private static final int PATTERN_ERROR = 0;
    private static final int EMPTY_ERROR = 1;
    private static final int NOT_SAME = 2;
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()._–[{}]:;',?/*~$^+=<>]).{8,16}$";
    final private String TAG = "LoginActivity";

    private RememberMePreferences rememberMePreferences;
    private UserDataPreferences userDataPreferences;
    private NetworkAvailable networkAvailable;
    private Intent intent;
    private User user;
    final ForgotPasswordContinuation[] forgotPasswordContinuation = new ForgotPasswordContinuation[1];

    private MyDatabase myDatabase;
    private SimpleItineraryDAO simpleItineraryDAO;
    private ItineraryDAOLambda itineraryDAOLambda;
    private UserDAOLambda userDAOLambda;

    private ProgressDialog progressDialog;
    private LoginFragment loginFragment;
    private ForgotPasswordEmailFragment forgotPasswordEmailFragment;
    private ForgotPasswordCodeFragment forgotPasswordCodeFragment;
    private ChangePasswordFragment changePasswordFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        rememberMePreferences = new RememberMePreferences(LoginActivity.this);
        userDataPreferences = new UserDataPreferences(LoginActivity.this);
        setMyDatabase();
        createFragments();
        checkFragmentToOpen();
    }

    /**
     * Checks which fragment to open, in base of Intent and RememberMePreferences.
     */
    public void checkFragmentToOpen() {
        intent = getIntent();
        if(intent.hasExtra("changepsw"))
            openChangePassword();
        else if(rememberMePreferences.checkRememberMe())
            openHome();
        else
            openLoginFragment();

    }

    @Override
    public void onBackPressed() {
        Log.i("USER_UI", "L'utente ha premuto il pulsante per tornare indietro in LoginActivity.");
        if(intent.hasExtra("changepsw")){
            Intent backHome = new Intent(this, HomeActivity.class);
            startActivity(backHome);
            finish();
        }
        else
            super.onBackPressed();
    }

    /**
     * Creates the fragments used.
     */
    public void createFragments() {
        Log.d(TAG, "Creazione dei fragment.");
        loginFragment = new LoginFragment();
        forgotPasswordEmailFragment = new ForgotPasswordEmailFragment();
        forgotPasswordCodeFragment = new ForgotPasswordCodeFragment();
        changePasswordFragment = new ChangePasswordFragment();
    }

    /**
     * Sets the fragment to LoginFragment.
     */
    public void openLoginFragment() {
        Log.d(TAG, "Apertura schermata login.");
        Log.i("USER_UI", "Aperta schermata login.");
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainerViewLogin, LoginFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Login the user if there is network connection.
     * It uses an instance of CognitoSettings to sign in the user.
     */
    public void loginUser() {
        Log.d(TAG, "Effettuando l'accesso.");
        Log.i("USER_UI", "Premuto pulsante login.");
        loginFragment = (LoginFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerViewLogin);

        String email = loginFragment.getEmail();
        if(email == null){
            loginFragment.setEmptyEmailError();
            return;
        }
        String password = loginFragment.getPassword();
        if(password == null){
            loginFragment.setEmptyPasswordError();
            return;
        }

        networkAvailable = new NetworkAvailable(LoginActivity.this);
        if(!networkAvailable.isNetworkAvailable()){
            networkAvailable.createAlertNoInternet();
            return;
        }

        CognitoSettings cognitoSettings = CognitoSettings.getCognitoInstance(this);
        CognitoUser thisUser = cognitoSettings.getUserPool().getUser(email);

        user = new User();
        user.setEmail(email);
        user.setPassword(password);

        loginFragment.setLoginDisabled();
        thisUser.signOut();

        Log.i("USER_UI", "L'utente visualizza una progress dialog di caricamento.");
        showProgressDialog();

        thisUser.getSessionInBackground(new AuthenticationHandler() {
            @Override
            public void onSuccess(CognitoUserSession userSession, CognitoDevice newDevice) {
                Log.i(TAG, "Login effettuato.");

                loginFragment.clear();
                loginFragment.setLoginEnabled();

                cognitoSettings.setLogin(userSession, user, new LoginResultCallback() {
                    @Override
                    public void onSuccess() {
                        //Log.d(TAG, "Credenziali inserite correttamente nell'Identity Pool.");
                        //getItinerariesList();
                        getUserAfterCredentials(user);
                    }

                    @Override
                    public void onFailure() {
                        Log.d(TAG, "Inserimento credenziali nell'Identity Pool fallito.");
                        Log.i("USER_UI", "L'utente ha finito di visualizzare la progress dialog di caricamento.");
                        progressDialog.dismiss();
                        logout(LoginActivity.this);
                        createAlertLoginError();
                    }
                });

            }

            @Override
            public void getAuthenticationDetails(AuthenticationContinuation authenticationContinuation, String userId) {
                AuthenticationDetails authenticationDetails = new AuthenticationDetails(userId, password, null);
                authenticationContinuation.setAuthenticationDetails(authenticationDetails);

                authenticationContinuation.continueTask();
            }

            @Override
            public void getMFACode(MultiFactorAuthenticationContinuation continuation) { }

            @Override
            public void authenticationChallenge(ChallengeContinuation continuation) { }

            @Override
            public void onFailure(Exception exception) {
                progressDialog.dismiss();
                Log.i("USER_UI", "L'utente ha finito di visualizzare la progress dialog di caricamento.");
                loginFragment.setLoginEnabled();

                if(exception.getClass().getCanonicalName().equals("com.amazonaws.services.cognitoidentityprovider.model.UserNotConfirmedException")){
                    Log.d(TAG, "L'utente deve ancora effettuare la conferma.");
                    Bundle extra = new Bundle();
                    extra.putString("email", email);
                    Intent verify = new Intent(LoginActivity.this, RegistrationActivity.class);
                    verify.putExtra("email", extra);
                    startActivity(verify);
                } else {
                    Log.d(TAG, "Login non riuscito.");
                    Log.i("USER_UI", "Visualizzato messaggio errore login.");
                    loginFragment.createErrorAlert(LoginActivity.this, AuthException.getMessage(Objects.requireNonNull(exception.getClass().getCanonicalName())));
                    rememberMePreferences.setRememberMeFalse();
                }

            }
        });
    }

    /**
     * Opens RegistrationActivity.
     */
    public void registerNewAccount() {
        Log.d(TAG, "Apertura RegistrationActivity.");
        Log.i("USER_UI", "Premuto pulsante Sign Up.");
        Intent newAccount = new Intent(LoginActivity.this, RegistrationActivity.class);
        startActivity(newAccount);
    }

    /**
     * Sets the fragment to ForgotPasswordEmailFragment.
     */
    public void openForgotPassword() {
        Log.d(TAG, "Apertura recupero password, sezione email.");
        Log.i("USER_UI", "Aperta schermata recupero password, sezione email.");
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainerViewLogin, ForgotPasswordEmailFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Changes remember me preferences.
     */
    public void rememberMe(boolean checked) {
        Log.d(TAG, "Cambiando remember me preferences.");
        Log.i("USER_UI", "L'utente ha cliccato sulla checkbox Remember Me");
        if(checked){
            rememberMePreferences.setRememberMeTrue();
        }
        else {
            rememberMePreferences.setRememberMeFalse();
        }
    }

    /**
     * Gets and sends verification code using user email.
     * Once the code will be sent, the user can change password.
     */
    public void getCode() {
        Log.d(TAG, "Mandando il codice all'utente.");
        Log.i("USER_UI", "L'utente clicca sul pulsante per ricevere il codice.");
        forgotPasswordEmailFragment = (ForgotPasswordEmailFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerViewLogin);

        String email = forgotPasswordEmailFragment.getEmail();
        if(email == null){
            forgotPasswordEmailFragment.setEmailError();
            return;
        }

        networkAvailable = new NetworkAvailable(LoginActivity.this);
        if(!networkAvailable.isNetworkAvailable()){
            networkAvailable.createAlertNoInternet();
            return;
        }

        forgotPasswordEmailFragment.setGetCodeDisabled();
        CognitoSettings cognitoSettings = CognitoSettings.getCognitoInstance(this);
        CognitoUser thisUser = cognitoSettings.getUserPool().getUser(email);

        thisUser.forgotPasswordInBackground(new ForgotPasswordHandler() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Cambio password effettuato con successo.");
                Log.i("USER_UI", "Visualizzata dialog di successo.");
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle(R.string.success_title)
                        .setMessage(getString(R.string.password_changed))
                        .setPositiveButton("OK", (dialog, which) -> {
                            forgotPasswordEmailFragment.setGetCodeEnabled();
                            forgotPasswordCodeFragment.setResetPasswordEnabled();
                            openLoginFragment();
                        })
                        .show();
            }

            @Override
            public void getResetCode(ForgotPasswordContinuation continuation) {
                CognitoUserCodeDeliveryDetails cognitoUserCodeDeliveryDetails = continuation.getParameters();
                Log.d(TAG, "Codice mandato a -> " + cognitoUserCodeDeliveryDetails.getDestination());
                Snackbar.make(findViewById(R.id.viewLogin), R.string.verification_code_sent, Snackbar.LENGTH_LONG).show();

                forgotPasswordContinuation[0] = continuation;

                openForgotPasswordCode();
            }

            @Override
            public void onFailure(Exception exception) {
                Log.d(TAG, "Cambio password non effettuato.");
                Log.i("USER_UI", "Visualizzato messaggio errore.");
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle(getString(R.string.try_again))
                        .setMessage(AuthException.getMessage(Objects.requireNonNull(exception.getClass().getCanonicalName())))
                        .setPositiveButton("OK", null)
                        .show();

                forgotPasswordEmailFragment.setGetCodeEnabled();
                forgotPasswordCodeFragment.setResetPasswordEnabled();
            }
        });
    }

    /**
     * Sets fragment to ForgotPasswordCodeFragment.
     */
    public void openForgotPasswordCode(){
        Log.d(TAG, "Apertura schmerata inserimento codice e password per il recupero dell'account.");
        Log.i("USER_UI", "Aperta schermata recupero password, sezione codice.");
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainerViewLogin, ForgotPasswordCodeFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Gets the verification code and the new password, sets them and continue the task.
     */
    public void resetPassword() {
        Log.d(TAG, "Effettuando il reset della password.");
        Log.i("USER_UI", "Premuto pulsante reset password.");
        forgotPasswordCodeFragment = (ForgotPasswordCodeFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerViewLogin);

        String code = forgotPasswordCodeFragment.getCode();
        if(code == null){
            forgotPasswordCodeFragment.setCodeError();
            return;
        }

        String password = forgotPasswordCodeFragment.getPassword();
        if(password == null){
            forgotPasswordCodeFragment.setPasswordError(EMPTY_ERROR);
            return;
        }

        final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        if(!matcher.matches()){
            forgotPasswordCodeFragment.setPasswordError(PATTERN_ERROR);
        }

        String confirmPassword = forgotPasswordCodeFragment.getConfirmPassword();
        if(confirmPassword == null){
            forgotPasswordCodeFragment.setConfirmPasswordError(EMPTY_ERROR);
            return;
        }

        if(!password.equals(confirmPassword)){
            forgotPasswordCodeFragment.setConfirmPasswordError(NOT_SAME);
            return;
        }

        networkAvailable = new NetworkAvailable(LoginActivity.this);
        if(!networkAvailable.isNetworkAvailable()){
            networkAvailable.createAlertNoInternet();
            return;
        }

        forgotPasswordCodeFragment.setResetPasswordDisabled();
        forgotPasswordContinuation[0].setPassword(password);
        forgotPasswordContinuation[0].setVerificationCode(code);

        forgotPasswordContinuation[0].continueTask();

    }

    /**
     * Sets fragment to ChangePasswordFragment.
     */
    public void openChangePassword() {
        Log.d(TAG, "Apertura cambio password.");
        Log.i("USER_UI", "Aperta schermata cambio password.");
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainerViewLogin, ChangePasswordFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Gets the old password, the new password and the confirm of the new password, checks them all
     * and changes the password.
     */
    public void changePassword() {
        Log.d(TAG, "Effettuando il cambiamento della password.");
        Log.i("USER_UI", "Premuto pulsante cambio password.");
        changePasswordFragment = (ChangePasswordFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerViewLogin);

        CognitoSettings cognitoSettings = CognitoSettings.getCognitoInstance(this);
        CognitoUser currentUser = cognitoSettings.getUserPool().getCurrentUser();

        String oldPassword = changePasswordFragment.getOldPassword();
        if(oldPassword == null){
            changePasswordFragment.setOldPasswordError();
            return;
        }

        String newPassword = changePasswordFragment.getNewPassword();
        if(newPassword == null){
            changePasswordFragment.setNewPasswordError(EMPTY_ERROR);
        }

        final Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(newPassword);
        if(!matcher.matches()){
            changePasswordFragment.setNewPasswordError(PATTERN_ERROR);
            return;
        }

        String confirmPassword = changePasswordFragment.getConfirmPassword();
        if(confirmPassword == null){
            changePasswordFragment.setConfirmPasswordError(EMPTY_ERROR);
            return;
        }

        if(!newPassword.equals(confirmPassword)){
            changePasswordFragment.setConfirmPasswordError(NOT_SAME);
            return;
        }

        networkAvailable = new NetworkAvailable(LoginActivity.this);
        if(!networkAvailable.isNetworkAvailable()){
            networkAvailable.createAlertNoInternet();
            return;
        }

        changePasswordFragment.setChangeButtonDisabled();

        currentUser.changePasswordInBackground(oldPassword, newPassword, new GenericHandler() {
            @Override
            public void onSuccess() {
                Log.d(TAG, "Password cambiata.");
                Log.d("UI_INTERACTION", "Visualizzato messaggio di cambio effettuato.");
                new AlertDialog.Builder(LoginActivity.this)
                        .setTitle(getString(R.string.done))
                        .setMessage(getString(R.string.password_changed_ok))
                        .setPositiveButton("OK", (dialog, which) -> {
                            changePasswordFragment.setChangeButtonEnabled();
                            finish();
                        })
                        .show();
            }

            @Override
            public void onFailure(Exception exception) {
                Log.d(TAG, "Password non cambiata.");
                Log.d("UI_INTERACTION", "Visualizzato messaggio di errore durante cambio effettuato.");
                changePasswordFragment.createErrorAlert(
                        LoginActivity.this,
                        AuthException.getMessage(Objects.requireNonNull(exception.getClass().getCanonicalName())));
                changePasswordFragment.setChangeButtonEnabled();
            }

        });


    }


    /**
     * Proceed to log out the user.
     * @param context context of the application.
     */
    public void logout(Context context){
        Log.d(TAG, "Logout.");
        Log.i("USER_UI", "L'utente effettua logout.");
        Thread thread = new Thread(() -> {
            CognitoSettings cognitoSettings = CognitoSettings.getCognitoInstance(context);
            CognitoUser thisUser = cognitoSettings.getUserPool().getCurrentUser();

            new RememberMePreferences(context).setRememberMeFalse();
            cognitoSettings.cognitoCachingCredentialsProvider.clear();
            clearInternalDb(context);

            thisUser.signOut();
        });
        thread.start();
    }

    /**
     * Opens HomeActivity.
     */
    public void openHome() {
        Log.d(TAG, "Apertura Home.");
        Log.i("USER_UI", "Apertura schermata principale.");
        Intent home = new Intent(LoginActivity.this, HomeActivity.class);
        startActivity(home);
        finish();
    }


    /**
     * Create AlertDialog when there is an error during login.
     */
    public void createAlertLoginError(){
        Log.d(TAG, "Visualizzazione messaggio di errore.");
        Log.i("USER_UI", "Visualizzazione messaggio di errore.");
        new AlertDialog.Builder(LoginActivity.this)
                .setTitle(getString(R.string.try_again))
                .setMessage(R.string.smtng_wrong_login)
                .setIcon(getDrawable(R.drawable.error_icon))
                .setPositiveButton("OK", (dialog, which) -> dialog.dismiss())
                .show();
    }


    /**
     * Clear and destroy internal db instance.
     * @param context context of the application
     */
    public void clearInternalDb(Context context){
        Log.d(TAG, "Svuotando database locale");
        MyDatabase myDatabase = MyDatabase.getInstance(context);
        SimpleItineraryDAO simpleItineraryDAO = myDatabase.simpleItineraryDAO();
        myDatabase.clearAllTables();
        MyDatabase.destroyInstance();
    }

    /**
     * Opens IntroActivity.
     */
    public void openIntro(){
        Log.d(TAG, "Apertura IntroActivity");
        Log.i("USER_UI", "Apertura schermata intro.");
        startActivity(new Intent(LoginActivity.this, IntroActivity.class));
        finish();
    }

    /**
     * Shows ProgressDialog.
     */
    public void showProgressDialog(){
        Log.d(TAG, "Impostando progress dialog.");
        progressDialog = new ProgressDialog(LoginActivity.this);
        progressDialog.show();
        progressDialog.setContentView(R.layout.progress_dialog_intro);
        progressDialog.getWindow().setBackgroundDrawableResource(R.color.transparent);

    }

    /**
     * Sets local database.
     */
    private void setMyDatabase(){
        Log.d(TAG, "Settando database locale.");
        myDatabase = MyDatabase.getInstance(LoginActivity.this);
        simpleItineraryDAO = myDatabase.simpleItineraryDAO();
    }

    /**
     * Recovers user itineraries after login.
     */
    public void getItinerariesList(){
        Log.d(TAG,"Recuperando liste.");
        ArrayList<SimpleItinerary> simpleItineraryArrayList = new ArrayList<>();

        ItineraryDAOLambda itineraryDAOLambda = ItineraryDAOLambda.getInstance();
        itineraryDAOLambda.getUserList(user.getEmail(), LoginActivity.this, new GetUserListCallback() {
            @Override
            public void onSuccess(ArrayList<SimpleItinerary> toVisitList, ArrayList<SimpleItinerary> favoriteList, ArrayList<SimpleItinerary> myItineraryList) {
                Log.d(TAG,"Recupero effettuato con successo.");
                Log.d("TAG", "List sizes are: " + toVisitList.size() + " - " + favoriteList.size() + " - " + myItineraryList.size());
                simpleItineraryArrayList.addAll(toVisitList);
                simpleItineraryArrayList.addAll(favoriteList);
                simpleItineraryArrayList.addAll(myItineraryList);

                for(int i=0; i<simpleItineraryArrayList.size(); i++)
                    MyDatabase.getInstance(LoginActivity.this).simpleItineraryDAO().insertSimpleItinerary(simpleItineraryArrayList.get(i));

                Log.i("USER_UI", "L'utente smette di visualizzare la progress dialog di caricamento.");
                progressDialog.dismiss();
                openIntro();
            }

            @Override
            public void onFailure() {
                Log.d(TAG,"Recupero fallito.");
                Log.i("USER_UI", "L'utente smette di visualizzare la progress dialog di caricamento.");
                progressDialog.dismiss();
                logout(LoginActivity.this);
                createAlertLoginError();
            }
        });



    }


    public void getUserAfterCredentials(User user){
        UserDAOLambda userDAOLambda = UserDAOLambda.getInstance();
        userDAOLambda.getUser(LoginActivity.this, user, new GetUserResultCallback() {
            @Override
            public void onSuccess(User returnUser) {
                userDataPreferences.setUserData(returnUser);
                getItinerariesList();
            }

            @Override
            public void onFailure() {
                progressDialog.dismiss();
                logout(LoginActivity.this);
                createAlertLoginError();
            }
        });
    }



}