package com.example.proconnect;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.proconnect.model.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class sign_up extends AppCompatActivity {

    EditText etusername, etpassword, etEmail;
    Button back, signup;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        etusername = findViewById(R.id.etUserName);
        etpassword = findViewById(R.id.etPassWord);
        etEmail = findViewById(R.id.etEmail);
        back = findViewById(R.id.btnBack);
        signup = findViewById(R.id.btnRealSignUp);

        back.setOnClickListener(v -> {
            Intent intent = new Intent(sign_up.this, login_screen.class);
            startActivity(intent);
        });

        signup.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = etEmail.getText().toString().trim();
        String password = etpassword.getText().toString().trim();
        String username = etusername.getText().toString().trim();

        if (username.isEmpty() || username.length() < 3) {
            etusername.setError("Username must be at least 3 characters");
            return;
        }
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Email and password are required", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(username, email);
                    } else {
                        Toast.makeText(sign_up.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveUserToFirestore(String username, String email) {
        String userId = mAuth.getCurrentUser().getUid();
        UserModel userModel = new UserModel(username, email, userId);

        db.collection("users").document(userId).set(userModel)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(sign_up.this, "User successfully registered!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(sign_up.this, MainActivity.class);
                        startActivity(intent);
                    } else {
                        Toast.makeText(sign_up.this, "Failed to save user data.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
