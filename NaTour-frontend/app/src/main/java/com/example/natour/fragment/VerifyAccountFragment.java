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


public class VerifyAccountFragment extends Fragment {

    private EditText code;
    private TextInputLayout codeLayout;
    private Button verify;

    private RegistrationActivity registrationActivity;

    public VerifyAccountFragment() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_verify_account, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        codeLayout = view.findViewById(R.id.codeVerifyAccountEditText);
        setLayout();
        verify = view.findViewById(R.id.verifyButton);
        verify.setOnClickListener(v -> ((RegistrationActivity) requireActivity()).verifyAccount());
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
     * Sets Layout of the fragment, setting listeners.
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
    }

    /**
     * Sets error in codeLayout.
     */
    public void setCodeError() {
        codeLayout.setError(getString(R.string.empty_code_error));
    }

    /**
     * Enables verify button.
     */
    public void setVerifyEnabled() {
        verify.setEnabled(true);
    }

    /**
     * Disables verify button.
     */
    public void setVerifyDisabled() {
        verify.setEnabled(false);
    }

    /**
     * Create error alert if there are problems.
     * @param context context of the application.
     * @param message message error.
     */
    public void createAlertError(Context context, String message) {
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.try_again))
                .setMessage(message)
                .setIcon(context.getDrawable(R.drawable.error_icon))
                .setPositiveButton("OK", null)
                .show();
    }


}