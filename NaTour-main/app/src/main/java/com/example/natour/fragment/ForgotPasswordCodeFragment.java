package com.example.natour.fragment;

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
import com.example.natour.boundary.LoginActivity;
import com.google.android.material.textfield.TextInputLayout;

public class ForgotPasswordCodeFragment extends Fragment {

    private static final int PATTERN_ERROR = 0;
    private static final int EMPTY_ERROR = 1;
    private static final int NOT_SAME = 2;

    private EditText code, newPassword, confirmPassword;
    private TextInputLayout codeLayout, newPasswordLayout, confirmPasswordLayout;
    private Button resetPassword;

    private LoginActivity loginActivity;

    public ForgotPasswordCodeFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password_code, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        codeLayout = view.findViewById(R.id.codeForgotPasswordEditText);
        newPasswordLayout = view.findViewById(R.id.newPasswordForgotPasswordEditText);
        confirmPasswordLayout = view.findViewById(R.id.confirmPasswordForgotPasswordEditText);
        setLayout();
        resetPassword = view.findViewById(R.id.resetPasswordButton);
        resetPassword.setOnClickListener(v -> ((LoginActivity) requireActivity()).resetPassword());
    }

    /**
     * Gets code from EditText.
     * @return code string or null.
     */
    public String getCode() {
        if(code.getText().toString() == null || code.getText().toString().equals(""))
            return null;
        else
            return code.getText().toString();
    }

    /**
     * Gets password from EditText.
     * @return password string or null.
     */
    public String getPassword() {
        if(newPassword.getText().toString() == null || newPassword.getText().toString().equals(""))
            return null;
        else
            return newPassword.getText().toString();
    }

    /**
     * Gets confirm password from EditText.
     * @return password string or null.
     */
    public String getConfirmPassword() {
        if(confirmPassword.getText().toString() == null || confirmPassword.getText().toString().equals(""))
            return null;
        else
            return confirmPassword.getText().toString();
    }

    /**
     * Sets layout, adding listeners.
     */
    public void setLayout(){
        code = codeLayout.getEditText();
        code.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                codeLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        newPassword = newPasswordLayout.getEditText();
        newPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                newPasswordLayout.setError(null);
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
     * Sets code error.
     */
    public void setCodeError() {
        codeLayout.setError(getString(R.string.empty_code_error));
    }

    /**
     * Sets password error.
     * @param errorType type of the error
     */
    public void setPasswordError(int errorType) {
        switch(errorType){
            case PATTERN_ERROR:
                newPasswordLayout.setError(getString(R.string.wrong_password_error));
                break;
            case EMPTY_ERROR:
                newPasswordLayout.setError(getString(R.string.empty_password_error));
                break;
        }
    }

    /**
     * Sets confirm password error.
     * @param errorType type of the error
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
     * Enables reset button.
     */
    public void setResetPasswordEnabled() {
        resetPassword.setEnabled(true);
    }

    /**
     * Disables reset button.
     */
    public void setResetPasswordDisabled() {
        resetPassword.setEnabled(false);
    }


}