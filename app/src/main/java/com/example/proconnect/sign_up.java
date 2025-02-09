package com.example.proconnect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.firestore.DocumentReference;

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    EditText etusername, etpassword, etEmail , etProfession, etLocation;
    Button back, signup, probtn, userbtn;
    boolean ispro = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etLocation = findViewById(R.id.etLocation);
        probtn = findViewById(R.id.btnPro);
        userbtn = findViewById(R.id.btnUser);
        etusername = findViewById(R.id.etUserName);
        etpassword = findViewById(R.id.etPassWord);
        etEmail = findViewById(R.id.etEmail);
        back = findViewById(R.id.btnBack);
        signup = findViewById(R.id.btnRealSignUp);
        etProfession = findViewById(R.id.etProffession);

        etLocation.setVisibility(View.GONE);
        etProfession.setVisibility(View.GONE);

        signup.setOnClickListener(this);
        back.setOnClickListener(this);
        probtn.setOnClickListener(this);
        userbtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            startActivity(new Intent(sign_up.this, login_screen.class));
        }
        if (v == probtn) {
            ispro = true;
            updateButtonColors();
            showProfessionField(true);
        }
        if (v == userbtn) {
            ispro = false;
            updateButtonColors();
            showProfessionField(false);
        }
        if (v == signup) {
            registerUser();
        }
    }

    private void showProfessionField(boolean show) {
        if (show) {
            etProfession.setVisibility(View.VISIBLE);
            etProfession.setAlpha(0f);
            etProfession.animate().alpha(1f).setDuration(500).start(); // Fade in

            etLocation.setVisibility(View.VISIBLE);
            etLocation.setAlpha(0f);
            etLocation.animate().alpha(1f).setDuration(500).start(); // Fade in
        } else {
            etProfession.animate().alpha(0f).setDuration(500).withEndAction(() -> etProfession.setVisibility(View.GONE)).start();
            etLocation.animate().alpha(0f).setDuration(500).withEndAction(() -> etProfession.setVisibility(View.GONE)).start(); // Fade out
        }
    }

    private void updateButtonColors() {
        if (ispro) {
            probtn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            userbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#606060")));
        } else {
            userbtn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            probtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#606060")));
        }
    }

    private void registerUser() {
        String name = etusername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etpassword.getText().toString().trim();
        String location = etLocation.getText().toString().trim();
        String profession = ispro ? etProfession.getText().toString().trim() : "None"; // 🆕 Get profession or set to "None"

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || (ispro && profession.isEmpty())) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    saveUserToFirestore(user.getEmail(), name, email, profession, location); // 🆕 Pass profession
                                } else {
                                    Toast.makeText(sign_up.this, "Failed to save name", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(sign_up.this, "Sign-up failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String safeEmail, String name, String email, String profession, String location) {
        String formattedEmail = email.replace("@", "_").replace(".", "_");

        // Create a new usermodel instance
        usermodel newUser = new usermodel(formattedEmail, name, email, ispro, profession, location, 0, 0); // 🆕 Initialize with 0 ratingSum and ratingCount

        // Save the user profile to Firestore
        firestore.collection("users").document(formattedEmail)
                .set(newUser)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(sign_up.this, "User saved to Firestore", Toast.LENGTH_SHORT).show();

                    // If the user is a pro, create the review document
                    if (ispro) {
                        // Create a new ReviewModel instance using the constructor
                        ReviewModel initialReview = new ReviewModel("No reviews yet!", email, 0f); // Default text, user email, default rating

                        // Reference to Firestore where the review will be saved
                        DocumentReference reviewDocRef = firestore.collection("reviews")
                                .document(formattedEmail) // Use formatted email as document ID
                                .collection("userReviews")
                                .document(); // This will generate a new unique review document ID

                        // Save the review document for the pro user
                        reviewDocRef.set(initialReview)
                                .addOnSuccessListener(aVoid1 -> {
                                    // Handle successful review creation
                                    Log.d("SignUp", "Review document created successfully!");
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure to create review
                                    Log.w("SignUp", "Error creating review document", e);
                                });
                    }

                    // Redirect to the main activity after successful sign-up
                    startActivity(new Intent(sign_up.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(sign_up.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show());

    }
}
