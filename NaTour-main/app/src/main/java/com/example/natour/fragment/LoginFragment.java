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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.example.natour.R;
import com.example.natour.boundary.LoginActivity;
import com.google.android.material.textfield.TextInputLayout;


public class LoginFragment extends Fragment {

    private EditText email;
    private EditText password;
    private TextInputLayout emailLayout;
    private TextInputLayout passwordLayout;
    private Button login;
    private CheckBox rememberMe;
    private TextView forgotPassword;
    private Button signUp;

    private LoginActivity loginActivity;

    public LoginFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        emailLayout = view.findViewById(R.id.emailLoginEditText);
        passwordLayout = view.findViewById(R.id.passwordLoginEditText);
        setLayouts();
        login = view.findViewById(R.id.loginButton);
        login.setOnClickListener(v -> ((LoginActivity) requireActivity()).loginUser());
        rememberMe = view.findViewById(R.id.rememberMeCheckBox);
        rememberMe.setOnClickListener(v -> {
            if(rememberMe.isChecked())
                ((LoginActivity) requireActivity()).rememberMe(true);
            else if(!rememberMe.isChecked())
                    ((LoginActivity) requireActivity()).rememberMe(false);

        });
        forgotPassword = view.findViewById(R.id.forgottenPasswordClickText);
        forgotPassword.setOnClickListener(v -> ((LoginActivity) requireActivity()).openForgotPassword());
        signUp = view.findViewById(R.id.newAccountButton);
        signUp.setOnClickListener(v -> ((LoginActivity) requireActivity()).registerNewAccount());

        super.onViewCreated(view, savedInstanceState);
    }

    /**
     * Gets email from EditText.
     * @return email string or null.
     */
    public String getEmail() {
        if(String.valueOf(email.getText()).equals("") || String.valueOf(email.getText()) == null)
            return null;
        return String.valueOf(email.getText());
    }

    /**
     * Gets password from EditText.
     * @return email string or null.
     */
    public String getPassword() {
        if(String.valueOf(password.getText()).equals("") || String.valueOf(password.getText()) == null){
            return null;
        }
        return String.valueOf(password.getText());
    }

    /**
     * Sets error in emailLayout.
     */
    public void setEmptyEmailError() {
        emailLayout.setError(getString(R.string.empty_email_error));
    }

    /**
     * Sets error in passwordLayout.
     */
    public void setEmptyPasswordError() {
        passwordLayout.setError(getString(R.string.empty_password_error));
    }

    /**
     * Sets the layout, adding listeners.
     */
    public void setLayouts(){
        email = emailLayout.getEditText();
        password = passwordLayout.getEditText();
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
    }

    /**
     * Clears the EditTexts.
     */
    public void clear() {
        email.setText("");
        password.setText("");
        rememberMe.setSelected(false);
    }

    /**
     * Create an error alert.
     * @param context context of the application.
     * @param message error message.
     */
    public void createErrorAlert(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.try_again))
                .setMessage(message)
                .setIcon(context.getDrawable(R.drawable.error_icon))
                .setPositiveButton("OK", null)
                .show();
    }

    /**
     * Enables login button.
     */
    public void setLoginEnabled() {
        login.setEnabled(true);
    }

    /**
     * Disables login button.
     */
    public void setLoginDisabled() {
        login.setEnabled(false);
    }


}