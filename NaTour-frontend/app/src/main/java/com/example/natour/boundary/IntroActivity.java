package com.example.natour.boundary;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.natour.R;

public class IntroActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_intro);
    }

    /**
     * Opens HomeActivity.
     */
    public void openHome(){
        Log.i("UI_INTERACTION", "Apertura schermata Home.");
        Intent home = new Intent(IntroActivity.this, HomeActivity.class);
        startActivity(home);
        finish();
    }


}