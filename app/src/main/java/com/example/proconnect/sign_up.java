package com.example.proconnect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    EditText etusername, etpassword, etEmail;
    Button back, signup ,probtn;
    boolean ispro = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance(); // Initialize Firestore
        probtn = findViewById(R.id.btnPro);
        etusername = findViewById(R.id.etUserName);
        etpassword = findViewById(R.id.etPassWord);
        etEmail = findViewById(R.id.etEmail);
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
        if(v == probtn){
            ispro = true;
            probtn.setBackgroundTintMode(PorterDuff.Mode.SRC_ATOP);
            probtn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            probtn.tint

        }
        if (v == signup) {
            String name = etusername.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etpassword.getText().toString().trim();

            mAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                FirebaseUser user = mAuth.getCurrentUser();

                                if (user != null) {
                                    // Update Auth Profile
                                    UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                            .setDisplayName(name)
                                            .build();

                                    user.updateProfile(profileUpdates)
                                            .addOnCompleteListener(task1 -> {
                                                if (task1.isSuccessful()) {
                                                    // Convert email to Firestore-safe ID
                                                    String safeEmail = email.replace("@", "_").replace(".", "_");

                                                    // Now store user info in Firestore
                                                    saveUserToFirestore(safeEmail, name, email);
                                                } else {
                                                    Toast.makeText(sign_up.this, "Failed to save name: " + task1.getException().getMessage(), Toast.LENGTH_LONG).show();
                                                }
                                            });
                                }
                            } else {
                                Toast.makeText(sign_up.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
        }
    }

    private void saveUserToFirestore(String safeEmail, String name, String email) {
        usermodel newUser = new usermodel(safeEmail, name, email, false); // Default isProfessional = false

        firestore.collection("users").document(safeEmail)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(sign_up.this, "User saved to Firestore", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(sign_up.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(sign_up.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show());
    }
}
