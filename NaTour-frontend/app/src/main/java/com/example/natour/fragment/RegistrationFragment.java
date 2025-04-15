package com.example.natour.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.natour.R;
import com.example.natour.boundary.RegistrationActivity;
import com.google.android.material.textfield.TextInputLayout;

public class RegistrationFragment extends Fragment {

    private static final int PATTERN_ERROR = 0;
    private static final int EMPTY_ERROR = 1;
    private static final int NOT_SAME = 2;

    private EditText name, surname, email, password, confirmPassword;
    private TextInputLayout nameLayout, surnameLayout, emailLayout, passwordLayout, confirmPasswordLayout;
    private Button register;

    private RegistrationActivity registrationActivity;

    public RegistrationFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_registration, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        nameLayout = view.findViewById(R.id.nameRegistrationEditText);
        surnameLayout = view.findViewById(R.id.surnameRegistrationEditText);
        emailLayout = view.findViewById(R.id.emailRegistrationEditText);
        passwordLayout = view.findViewById(R.id.passwordRegistrationEditText);
        confirmPasswordLayout = view.findViewById(R.id.confrimPasswordRegistrationEditText);
        setLayout();
        register = view.findViewById(R.id.registrationButton);
        register.setOnClickListener(v -> ((RegistrationActivity) requireActivity()).registerUser());
    }

    /**
     * Gets name from EditText.
     * @return name string or null.
     */
    public String getName() {
        if(name.getText().toString() == null || name.getText().toString().equals(""))
            return null;
        else
            return name.getText().toString();
    }

    /**
     * Gets surname from EditText.
     * @return surname string or null.
     */
    public String getSurname() {
        if(surname.getText().toString() == null || surname.getText().toString().equals(""))
            return null;
        else
            return surname.getText().toString();
    }

    /**
     * Gets email from EditText.
     * @return email string or null.
     */
    public String getEmail() {
        if(email.getText().toString() == null || email.getText().toString().equals(""))
            return null;
        else
            return email.getText().toString();
    }

    /**
     * Gets password from EditText.
     * @return password string or null.
     */
    public String getPassword() {
        if(password.getText().toString() == null || password.getText().toString().equals(""))
            return null;
        else
            return password.getText().toString();
    }

    /**
     * Gets confirm password from EditText.
     * @return confirm password string or null.
     */
    public String getConfirmPassword() {
        if(confirmPassword.getText().toString() == null || confirmPassword.getText().toString().equals(""))
            return null;
        else
            return confirmPassword.getText().toString();
    }

    /**
     * Sets Layout of the fragment adding listeners.
     */
    public void setLayout(){
        name = nameLayout.getEditText();
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                nameLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        surname = surnameLayout.getEditText();
        surname.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                surnameLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        email = emailLayout.getEditText();
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                emailLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        password = passwordLayout.getEditText();
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                passwordLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        confirmPassword = confirmPasswordLayout.getEditText();
        confirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                confirmPasswordLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * Enables register button.
     */
    public void setRegisterEnabled() {
        register.setEnabled(true);
    }

    /**
     * Disables register button.
     */
    public void setRegisterDisabled() {
        register.setEnabled(false);
    }

    /**
     * Sets error in nameLayout.
     * @param errorType type of the error.
     */
    public void setNameError(int errorType) {
        switch(errorType){
            case PATTERN_ERROR:
                nameLayout.setError(getString(R.string.wrong_name_error));
                break;
            case EMPTY_ERROR:
                nameLayout.setError(getString(R.string.empty_name_error));
                break;
        }

    }

    /**
     * Sets error in surnameLayout.
     * @param errorType type of the error.
     */
    public void setSurnameError(int errorType) {
        switch(errorType){
            case PATTERN_ERROR:
                surnameLayout.setError(getString(R.string.wrong_surname_error));
                break;
            case EMPTY_ERROR:
                surnameLayout.setError(getString(R.string.empty_surname_error));
                break;
        }

    }

    /**
     * Sets error in emailLayout.
     */
    public void setEmailError() {
        emailLayout.setError(getString(R.string.empty_email_error));
    }

    /**
     * Sets error in passwordLayout.
     * @param errorType type of the error.
     */
    public void setPasswordError(int errorType) {
        switch(errorType){
            case PATTERN_ERROR:
                passwordLayout.setError(getString(R.string.wrong_password_error));
                break;
            case EMPTY_ERROR:
                passwordLayout.setError(getString(R.string.empty_password_error));
                break;
        }
    }

    /**
     * Sets error in confirmPasswordLayout.
     * @param errorType type of the error.
     */
    public void setConfirmPasswordError(int errorType) {
        switch(errorType){
            case NOT_SAME:
                confirmPasswordLayout.setError(getString(R.string.confirm_password_error));
                break;
            case EMPTY_ERROR:
                confirmPasswordLayout.setError(getString(R.string.empty_confirm_password_error));
                break;
        }
    }

    /**
     * Creates AlertDialog to show an error.
     * @param context context of the application.
     * @param message message of error.
     */
    public void createErrorAlert(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle("Try Again")
                .setIcon(context.getDrawable(R.drawable.error_icon))
                .setMessage(message)
                .setPositiveButton("OK", null)
                .show();
    }

}