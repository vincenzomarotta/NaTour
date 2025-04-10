package com.example.natour.boundary;

import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUser;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserAttributes;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.CognitoUserSession;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.GenericHandler;
import com.amazonaws.mobileconnectors.cognitoidentityprovider.handlers.SignUpHandler;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentityprovider.model.SignUpResult;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.InvokeRequest;
import com.amazonaws.services.lambda.model.InvokeResult;
import com.amazonaws.services.lambda.model.ServiceException;
import com.example.natour.R;
import com.example.natour.entity.User;
import com.example.natour.exceptions.AuthException;
import com.example.natour.exceptions.EmailNotCorrectException;
import com.example.natour.exceptions.NameNotCorrectException;
import com.example.natour.exceptions.PasswordNotCorrectException;
import com.example.natour.exceptions.SignUpException;
import com.example.natour.exceptions.SurnameNotCorrectException;
import com.example.natour.fragment.RegistrationFragment;
import com.example.natour.fragment.VerifyAccountFragment;
import com.example.natour.utils.CognitoSettings;
import com.example.natour.utils.NetworkAvailable;
import com.example.natour.utils.UserDataPreferences;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegistrationActivity extends AppCompatActivity {

    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()._–[{}]:;',?/*~$^+=<>]).{8,16}$";
    private static final String NAME_SURNAME_PATTERN_ERROR = "[\\p{Punct}+[£°ç§^]+{0-9}]";
    private static final int PATTERN_ERROR = 0;
    private static final int EMPTY_ERROR = 1;
    public static final int NOT_SAME = 2;
    final private String SUCCESS = "DONE";
    private final String TAG = "RegistrationActivity";

    private User newUser = new User();
    private NetworkAvailable networkAvailable;

    private RegistrationFragment registrationFragment = new RegistrationFragment();
    private VerifyAccountFragment verifyAccountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);
        createFragments();
        checkFragmentToOpen();
    }

    /**
     * Checks which fragment to open first in base of Intent.
     */
    public void checkFragmentToOpen() {
        Bundle getEmail = getIntent().getBundleExtra("email");
        if(getEmail != null){
            newUser.setEmail(getEmail.getString("email"));
            openVerifyAccount();
        } else {
            openRegistrationFragment();
        }
    }

    /**
     * Proceeds to create all the fragments.
     */
    public void createFragments() {
        registrationFragment = new RegistrationFragment();
        verifyAccountFragment = new VerifyAccountFragment();
    }

    @Override
    public void onBackPressed() {
        Log.i("UI_INTERACTION","Premuto pulsante per tornare indietro in RegistrationActivity.");
        super.onBackPressed();
    }

    /**
     * Sets fragment to RegistrationFragment.
     */
    public void openRegistrationFragment() {
        Log.d(TAG, "Apertura registrazione");
        Log.i("UI_INTERACTION","Apertura schermata registrazione.");
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainerViewRegistration, RegistrationFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Proceeds to register the user.
     */
    public void registerUser() {
        Log.i("UI_INTERACTION","Premuto pulsante per la registrazione.");
        Log.d(TAG, "Registrazione.");
        registrationFragment = (RegistrationFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerViewRegistration);

        try{
            newUser = createUser();
            Log.d(TAG, newUser.toString());
        } catch(Exception e){
            Log.d(TAG, "Eccezione -> " + e.getMessage());
            return;
        }

        networkAvailable = new NetworkAvailable(RegistrationActivity.this);
        if(!networkAvailable.isNetworkAvailable()){
            networkAvailable.createAlertNoInternet();
            return;
        }

        registrationFragment.setRegisterDisabled();

        final CognitoUserAttributes userAttributes = new CognitoUserAttributes();
        userAttributes.addAttribute("custom:name", newUser.getName());
        userAttributes.addAttribute("custom:surname", newUser.getSurname());
        userAttributes.addAttribute("email", newUser.getEmail());


        CognitoSettings cognitoSettings = CognitoSettings.getCognitoInstance(RegistrationActivity.this);
        cognitoSettings.getUserPool().signUpInBackground(
                newUser.getEmail(),
                newUser.getPassword(),
                userAttributes,
                null,
                new SignUpHandler() {
                    @Override
                    public void onSuccess(CognitoUser user, SignUpResult signUpResult) {

                        if(!signUpResult.isUserConfirmed()){
                            Log.d(TAG, "L'utente ha effettuato la registrazione.");
                            Log.i("UI_INTERACTION","L'utente visualizza snackbar.");

                            Snackbar.make(findViewById(R.id.viewRegistration), R.string.verification_code_sent, Snackbar.LENGTH_LONG).show();
                            new UserDataPreferences(RegistrationActivity.this).setUserData(newUser);

                            registrationFragment.setRegisterEnabled();
                            openVerifyAccount();
                        }
                        else {
                            Log.i("UI_INTERACTION","L'utente visualizza una dialog di completamento della registrazione.");
                            new AlertDialog.Builder(RegistrationActivity.this)
                                    .setTitle(getString(R.string.success_title))
                                    .setMessage(getString(R.string.registration_completed))
                                    .setPositiveButton("OK", (dialog, which) -> {
                                        registrationFragment.setRegisterEnabled();
                                        finish();
                                    })
                                    .show();
                        }
                    }

                    @Override
                    public void onFailure(Exception exception) {
                        Log.d(TAG, "Registrazione non riuscita");
                        registrationFragment.setRegisterEnabled();
                        Log.i("UI_INTERACTION","Utente visualizza messaggio di errore.");
                        registrationFragment.createErrorAlert(
                                RegistrationActivity.this,
                                SignUpException.getMessage(Objects.requireNonNull(exception.getClass().getCanonicalName())));
                        return;
                    }
                });

    }

    /**
     * Creates a new user.
     * This method get user attributes, checks them and return a new user.
     * If one of the parameters is not correct, this method throws and Exception that will be
     * cached in the method #registerUser() and blocks the registration.
     * @return new User
     * @throws Exception
     */
    public User createUser() throws Exception {
        User user = new User();

        final Pattern pattenNameSurname = Pattern.compile(NAME_SURNAME_PATTERN_ERROR);
        final Pattern patternPassword = Pattern.compile(PASSWORD_PATTERN);
        Log.d(TAG, "Creazione user.");

        user.setName(registrationFragment.getName());
        if(user.getName() == null){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo nome.");
            registrationFragment.setNameError(EMPTY_ERROR);
            throw new NameNotCorrectException();
        }

        Matcher matcherName = pattenNameSurname.matcher(user.getName());
        if(matcherName.find()){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo nome.");
            registrationFragment.setNameError(PATTERN_ERROR);
            throw new NameNotCorrectException();
        }

        user.setSurname(registrationFragment.getSurname());
        if(user.getSurname() == null){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo cognome.");
            registrationFragment.setSurnameError(EMPTY_ERROR);
            throw new SurnameNotCorrectException();
        }

        Matcher matcherSurname = pattenNameSurname.matcher(user.getSurname());
        if(matcherSurname.find()){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo cognome.");
            registrationFragment.setSurnameError(PATTERN_ERROR);
            throw new SurnameNotCorrectException();
        }

        user.setEmail(registrationFragment.getEmail());
        if(user.getEmail() == null){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo email.");
            registrationFragment.setEmailError();
            throw new EmailNotCorrectException();
        }

        user.setPassword(registrationFragment.getPassword());
        if(user.getPassword() == null){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo password.");
            registrationFragment.setPasswordError(EMPTY_ERROR);
            throw new PasswordNotCorrectException();
        }

        Matcher matcherPassword = patternPassword.matcher(user.getPassword());
        if(!matcherPassword.matches()){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo password.");
            registrationFragment.setPasswordError(PATTERN_ERROR);
            throw new PasswordNotCorrectException();
        }

        String confirmPassword = registrationFragment.getConfirmPassword();
        if(confirmPassword == null){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo conferma password.");
            registrationFragment.setConfirmPasswordError(EMPTY_ERROR);
            throw new PasswordNotCorrectException();
        }

        if(!user.getPassword().equals(confirmPassword)){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo conferma password.");
            registrationFragment.setConfirmPasswordError(NOT_SAME);
            throw new PasswordNotCorrectException();
        }

        return user;
    }

    /**
     *  Sets fragment tp VerifyAccountFragment.
     */
    public void openVerifyAccount() {
        Log.d(TAG, "Apertura verifica account.");
        Log.i("UI_INTERACTION","Apertura schermata verifica utente.");
        getSupportFragmentManager().beginTransaction().setReorderingAllowed(true)
                .replace(R.id.fragmentContainerViewRegistration, VerifyAccountFragment.class, null)
                .addToBackStack(null)
                .commit();
    }

    /**
     * Proceeds to verify the user using the email and the asynctask to confirm the code.
     */
    public void verifyAccount() {
        Log.d(TAG, "Verifica account");
        Log.i("UI_INTERACTION","Premuto pulsante verifica.");
        verifyAccountFragment = (VerifyAccountFragment) getSupportFragmentManager().findFragmentById(R.id.fragmentContainerViewRegistration);

        String code = verifyAccountFragment.getCode();
        if(code == null){
            Log.i("UI_INTERACTION","Visualizzato errore nel campo conferma codice.");
            verifyAccountFragment.setCodeError();
            return;
        }

        networkAvailable = new NetworkAvailable(RegistrationActivity.this);
        if(!networkAvailable.isNetworkAvailable()){
            networkAvailable.createAlertNoInternet();
            return;
        }

        verifyAccountFragment.setVerifyDisabled();
        new ConfirmTask().execute(code, newUser.getEmail());
    }

    /**
     * Private class that extends AsyncTask used to check if the insert code is correct.
     */
    private class ConfirmTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... strings) {
            final String[] result = new String[1];
            Log.d(TAG, "Controllo codice.");

            CognitoSettings cognitoSettings = CognitoSettings.getCognitoInstance(RegistrationActivity.this);
            CognitoUser thisUser = cognitoSettings.getUserPool().getUser(strings[1]);

            thisUser.confirmSignUp(strings[0], false, new GenericHandler() {
                @Override
                public void onSuccess() {
                    result[0] = SUCCESS;
                }

                @Override
                public void onFailure(Exception exception) {
                    result[0] = SignUpException.getMessage(Objects.requireNonNull(exception.getClass().getCanonicalName()));
                }
            });

            return result[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.i("UI_INTERACTION","Visualizzata dialog di successo.");
            if(result.equals(SUCCESS)){
                Log.d(TAG,"Codice confermato.");
                new AlertDialog.Builder(RegistrationActivity.this)
                        .setTitle(getString(R.string.success_title))
                        .setMessage(getString(R.string.registration_completed))
                        .setPositiveButton("OK", (dialog, which) -> {
                            finish();
                        })
                        .show();

            }
            else{
                Log.d(TAG,"Codice non valido.");
                Log.i("UI_INTERACTION","Visualizzata dialog di errore.");
                verifyAccountFragment.createAlertError(RegistrationActivity.this, result);
            }

            Log.d(TAG, "Risultato conferma -> " + result);
            verifyAccountFragment.setVerifyEnabled();
        }

    }


}