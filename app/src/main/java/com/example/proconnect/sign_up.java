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
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

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
        String formattedEmail = email.toLowerCase().replace("@", "_").replace(".", "_");

        // Create a new usermodel instance
        UserModel newUser = new UserModel(formattedEmail, name, email, ispro, profession, location); // 🆕 Initialize with profession

        // Save the user profile to Firestore
        firestore.collection("users").document(formattedEmail)
                .set(newUser)
                .addOnSuccessListener(result -> {
                    Toast.makeText(sign_up.this, "User saved to Firestore", Toast.LENGTH_SHORT).show();

                    // If the user is a professional, create their review data
                    if (ispro) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();

                        // Create a new document in the "reviews" collection using the formatted email as the ID
                        DocumentReference userReviewDoc = db.collection("reviews").document(formattedEmail);

                        // Set initial review data for the professional (rating, sum, count)
                        Map<String, Object> professionalData = new HashMap<>();
                        professionalData.put("rating", 0f);         // Default rating is 0
                        professionalData.put("ratingsum", 0);      // Initial sum of ratings
                        professionalData.put("ratingcount", 0);    // No ratings yet

                        // Save the professional's review data to Firestore
                        userReviewDoc.set(professionalData)
                                .addOnSuccessListener(innerResult -> {
                                    Log.d("SignUp", "Professional review data created successfully");

                                    // Now create the "reviewspost" subcollection under the user's review document
                                    CollectionReference reviewspostRef = userReviewDoc.collection("reviewspost");

                                    // Create an example review (you can leave this part out once the reviews start coming)
                                    Map<String, Object> initialReview = new HashMap<>();
                                    initialReview.put("username", "admin"); // Example username
                                    initialReview.put("reviewText", "Initial review");  // Example review text
                                    initialReview.put("rating", 5); // Example rating (5 stars)

                                    // Adding the dummy review to the "reviewspost" subcollection
                                    reviewspostRef.document("initial_review") // Name it something like "initial_review"
                                            .set(initialReview)
                                            .addOnSuccessListener(dummyResult -> {
                                                Log.d("SignUp", "Initial review added to reviewspost");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("SignUp", "Error adding initial review to reviewspost: " + e.getMessage());
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SignUp", "Error creating professional review data: " + e.getMessage());
                                    Toast.makeText(sign_up.this, "Error creating professional review data", Toast.LENGTH_LONG).show();
                                });
                    }

                    // Redirect to the main activity after successful sign-up
                    startActivity(new Intent(sign_up.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(sign_up.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show());


    }
}
