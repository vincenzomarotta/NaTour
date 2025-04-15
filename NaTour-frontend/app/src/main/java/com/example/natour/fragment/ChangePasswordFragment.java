package com.example.natour.fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.natour.R;
import com.example.natour.boundary.LoginActivity;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Objects;

public class ChangePasswordFragment extends Fragment {

    private static final int PATTERN_ERROR = 0;
    private static final int EMPTY_ERROR = 1;
    private static final int NOT_SAME = 2;

    private EditText oldPassword, newPassword, confirmPassword;
    private TextInputLayout oldPasswordLayout, newPasswordLayout, confirmPasswordLayout;
    private Button changePassword;

    private LoginActivity loginActivity;

    public ChangePasswordFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_change_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        oldPasswordLayout = view.findViewById(R.id.oldPasswordChangePasswordEditText);
        newPasswordLayout = view.findViewById(R.id.newPasswordChangePasswordEditText);
        confirmPasswordLayout = view.findViewById(R.id.confirmNewPasswordChangePasswordEditText);
        setLayout();
        changePassword = view.findViewById(R.id.changePasswordButton);
        changePassword.setOnClickListener(v -> ((LoginActivity) requireActivity()).changePassword());
    }

    /**
     * Gets old password.
     * @return old password string or null.
     */
    public String getOldPassword() {
        if(oldPassword.getText().toString() == null || oldPassword.getText().toString().equals(""))
            return null;
        else
            return oldPassword.getText().toString();
    }

    /**
     * Gets new password.
     * @return new password string or null.
     */
    public String getNewPassword() {
        if(newPassword.getText().toString() == null || newPassword.getText().toString().equals(""))
            return null;
        else
            return newPassword.getText().toString();
    }

    /**
     * Gets confirm password.
     * @return confirm password string or null.
     */
    public String getConfirmPassword() {
        if(confirmPassword.getText().toString() == null || confirmPassword.getText().toString().equals(""))
            return null;
        else
            return confirmPassword.getText().toString();
    }

    /**
     * Sets error in oldPasswordLayout.
     */
    public void setOldPasswordError() {
        oldPasswordLayout.setError(getString(R.string.empty_old_password));
    }

    /**
     * Sets layout of the fragment, adding listeners.
     */
    public void setLayout(){
        oldPassword = oldPasswordLayout.getEditText();
        oldPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                oldPasswordLayout.setError(null);
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
     * Sets error in newPasswordLayout.
     * @param errorType type of the error.
     */
    public void setNewPasswordError(int errorType) {
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
     * Enables change password button.
     */
    public void setChangeButtonEnabled() {
        changePassword.setEnabled(true);
    }

    /**
     * Disables change password button.
     */
    public void setChangeButtonDisabled() {
        changePassword.setEnabled(false);
    }

    /**
     * Creates AlertDialog of error.
     * @param context context of the application.
     * @param message message error.
     */
    public void createErrorAlert(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.try_again))
                .setMessage(message)
                .setIcon(context.getDrawable(R.drawable.error_icon))
                .setPositiveButton("OK", null)
                .show();
    }


}