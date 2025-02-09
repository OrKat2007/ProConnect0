package com.example.proconnect;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class startup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user != null) {
            // User is logged in, go to MainActivity and finish this activity
            Intent intent = new Intent(startup.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            // Start a delayed thread only if user is NOT logged in
            new Thread(() -> {
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                    Intent i = new Intent(startup.this, login_screen.class);
                    startActivity(i);
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }
}
