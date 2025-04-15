package com.example.natour.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.natour.R;
import com.example.natour.boundary.LoginActivity;
import com.google.android.material.textfield.TextInputLayout;


public class ForgotPasswordEmailFragment extends Fragment {

    private EditText email;
    private TextInputLayout emailLayout;
    private Button getCode;

    private LoginActivity loginActivity;

    public ForgotPasswordEmailFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password_email, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        emailLayout = view.findViewById(R.id.emailForgotPasswordEditText);
        setLayout();
        getCode = view.findViewById(R.id.getCodeButton);
        getCode.setOnClickListener(v -> ((LoginActivity) requireActivity()).getCode());
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
     * Sets layout adding listeners.
     */
    public void setLayout(){
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
    }

    /**
     * Sets email error in emailLayout.
     */
    public void setEmailError() {
        emailLayout.setError(getString(R.string.empty_email_error));
    }

    /**
     * Enables get code button.
     */
    public void setGetCodeEnabled() {
        getCode.setEnabled(true);
    }

    /**
     * Disables get code button.
     */
    public void setGetCodeDisabled() {
        getCode.setEnabled(false);
    }

}