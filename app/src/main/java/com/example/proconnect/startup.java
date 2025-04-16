package com.example.proconnect;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class startup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);

        // Splash delay thread
        new Thread(() -> {
            try {
                Thread.sleep(1000); // Show splash for 3 seconds
                runOnUiThread(() -> {
                    if (!isNetworkAvailable(startup.this)) {
                        showNoConnectionDialog();
                    } else {
                        proceedToNextScreen();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void proceedToNextScreen() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        Intent intent;
        if (user != null) {
            intent = new Intent(startup.this, MainActivity.class);
        } else {
            intent = new Intent(startup.this, login_screen.class);
        }
        startActivity(intent);
        finish();
    }

    private boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            return activeNetwork != null && activeNetwork.isConnected();
        }
        return false;
    }

    private void showNoConnectionDialog() {
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.custom_no_connection, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setCancelable(false);

        final AlertDialog dialog = builder.create();

        Button btnReconnect = dialogView.findViewById(R.id.btnReconnect);
        btnReconnect.setOnClickListener(v -> {
            if (isNetworkAvailable(this)) {
                dialog.dismiss();
                proceedToNextScreen();
            } else {
                Toast.makeText(this, "Still no connection", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                showNoConnectionDialog();
            }
        });

        dialog.show();
    }
}
