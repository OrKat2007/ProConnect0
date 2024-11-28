package com.example.proconnect;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class startup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        Thread t = new Thread() {
            public void run() {

                try {
//sleep thread for 10 seconds, time in milliseconds
                    sleep(2600);

//start new activity
                    Intent i = new Intent(startup.this, MainActivity.class);
                    startActivity(i);

//destroying Splash activity
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

//start thread
        t.start();
    }
}