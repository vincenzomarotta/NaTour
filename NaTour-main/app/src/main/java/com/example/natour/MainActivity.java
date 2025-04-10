package com.example.natour;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.example.natour.boundary.HomeActivity;
import com.example.natour.boundary.IntroActivity;
import com.example.natour.boundary.LoginActivity;
import com.example.natour.utils.LogcatToFile;
import com.example.natour.utils.LogcatToFileBuilder;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            LogcatToFile logcatToFile = LogcatToFileBuilder.newBuilder(this)
                    .fileName("logcat")
                    .addTag("UI_INTERACTION")
                    .build();
            logcatToFile.execute();
        } catch (IOException e) {
            Log.d("IO_EXCEPTION", e.getMessage());
        }

        //openLogin();


        final Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            openLogin();
        }, 3500);


    }

    public void openLogin(){
        Intent i = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(i);
        finish();
    }


}