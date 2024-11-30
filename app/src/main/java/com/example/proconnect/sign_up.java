package com.example.proconnect;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.analytics.FirebaseAnalytics;

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    EditText etusername, etpassword;
    Button back, signup;

    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        etusername = findViewById(R.id.etUserName);
        etpassword = findViewById(R.id.etPassWord);
        back = findViewById(R.id.btnBack);
        signup = findViewById(R.id.btnRealSignUp);

        signup.setOnClickListener(this);
        back.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            Intent intent = new Intent(sign_up.this, login_screen.class);
            startActivity(intent);
        }
        if (v == signup) {
            saveNameToFirebaseAnalytics();
        }
    }

    private void saveNameToFirebaseAnalytics() {
        String name = etusername.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Please enter a name", Toast.LENGTH_SHORT).show();
            return;
        }

        Bundle bundle = new Bundle();
        bundle.putString("username", name);
        mFirebaseAnalytics.logEvent("user_signup", bundle);

        Intent intent = new Intent(sign_up.this, MainActivity.class);
        startActivity(intent);
    }
}
