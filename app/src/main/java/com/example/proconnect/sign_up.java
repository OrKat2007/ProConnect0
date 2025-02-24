package com.example.proconnect;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.proconnect.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    private EditText etusername, etpassword, etEmail, etAge, etLanguage;
    private Button back, signup, probtn, userbtn;
    private boolean isPro = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    int ageInt = 0;

    // Variables to store professional details from the dialog
    private String proProfession = "";
    private String proLocation = "";
    private String proAvailability = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        // Main fields
        etusername = findViewById(R.id.etUserName);
        etpassword = findViewById(R.id.etPassWord);
        etEmail = findViewById(R.id.etEmail);
        etAge = findViewById(R.id.etAge);
        etLanguage = findViewById(R.id.etLanguage);

        // Buttons
        probtn = findViewById(R.id.btnPro);
        userbtn = findViewById(R.id.btnUser);
        signup = findViewById(R.id.btnRealSignUp);
        back = findViewById(R.id.btnBack);

        // Click listeners
        signup.setOnClickListener(this);
        back.setOnClickListener(this);
        probtn.setOnClickListener(this);
        userbtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == back) {
            startActivity(new Intent(sign_up.this, login_screen.class));
            finish();
        }
        if (v == probtn) {
            isPro = true;
            updateButtonColors();
            // Show the dialog for professional details
            showProfessionalDialog();
        }
        if (v == userbtn) {
            isPro = false;
            updateButtonColors();
            // Clear out any previously stored professional data
            proProfession = "";
            proLocation = "";
            proAvailability = "";
        }
        if (v == signup) {
            registerUser();
        }
    }

    private void updateButtonColors() {
        if (isPro) {
            probtn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            userbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#606060")));
        } else {
            userbtn.setBackgroundTintList(ColorStateList.valueOf(Color.GREEN));
            probtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#606060")));
        }
    }

    /**
     * Show a dialog to get profession, location, and availability for professional users.
     */
    private void showProfessionalDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_professional_details, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);

        final EditText etDialogProfession = dialogView.findViewById(R.id.etDialogProfession);
        final EditText etDialogLocation = dialogView.findViewById(R.id.etDialogLocation);
        final EditText etDialogAvailability = dialogView.findViewById(R.id.etDialogAvailability);
        Button btnDialogOk = dialogView.findViewById(R.id.btnDialogOk);

        AlertDialog dialog = builder.create();

        btnDialogOk.setOnClickListener(view -> {
            proProfession = etDialogProfession.getText().toString().trim();
            proLocation = etDialogLocation.getText().toString().trim();
            String rawAvailability = etDialogAvailability.getText().toString().trim();

            // Normalize availability assuming it's entered in 24-hour format.
            // For example, "9 - 19" becomes "09:00-19:00"
            proAvailability = normalizeAvailability(rawAvailability);

            // Basic validation
            if (proProfession.isEmpty() || proLocation.isEmpty() || proAvailability.isEmpty()) {
                Toast.makeText(sign_up.this, "Please fill all professional fields", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void registerUser() {
        String name = etusername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etpassword.getText().toString().trim();
        String ageStr = etAge.getText().toString().trim();
        String rawLanguages = etLanguage.getText().toString().trim();

        // Normalize languages input: remove commas and extra spaces.
        String normalizedLanguages = normalizeLanguages(rawLanguages);

        try {
            if (!ageStr.isEmpty()) {
                ageInt = Integer.parseInt(ageStr);
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid age format", Toast.LENGTH_SHORT).show();
            return;
        }

        // If user is a professional but professional details are not filled, show dialog again.
        if (isPro && (proProfession.isEmpty() || proLocation.isEmpty() || proAvailability.isEmpty())) {
            Toast.makeText(this, "Please fill professional details", Toast.LENGTH_SHORT).show();
            showProfessionalDialog();
            return;
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || normalizedLanguages.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create user in Firebase Auth
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            // Update display name in Firebase Auth
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(name)
                                    .build();

                            user.updateProfile(profileUpdates).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    // Save all data to Firestore
                                    saveUserToFirestore(
                                            user.getEmail(),
                                            name,
                                            email,
                                            ageInt,
                                            normalizedLanguages
                                    );
                                } else {
                                    Toast.makeText(sign_up.this, "Failed to save name", Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                    } else {
                        Toast.makeText(sign_up.this, "Sign-up failed: "
                                + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void saveUserToFirestore(String safeEmail, String name, String email,
                                     int age, String languages) {

        String formattedEmail = email.toLowerCase().replace("@", "_").replace(".", "_");

        // For normal users, set placeholders for professional fields.
        String finalProfession = isPro ? proProfession : "None";
        String finalLocation = isPro ? proLocation : "None";
        String finalAvailability = isPro ? proAvailability : "None";

        // Create a new user model instance.
        UserModel newUser = new UserModel(
                formattedEmail,
                name,
                email,
                isPro,
                finalProfession,
                finalLocation
        );

        // Set additional fields.
        newUser.setAge(age);
        newUser.setLanguages(languages);
        newUser.setAvailability(finalAvailability);

        firestore.collection("users").document(formattedEmail)
                .set(newUser)
                .addOnSuccessListener(result -> {
                    Toast.makeText(sign_up.this, "User saved to Firestore", Toast.LENGTH_SHORT).show();

                    // If the user is a professional, initialize review data.
                    if (isPro) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userReviewDoc = db.collection("reviews").document(formattedEmail);

                        Map<String, Object> professionalData = new HashMap<>();
                        professionalData.put("rating", 0f);
                        professionalData.put("ratingsum", 0);
                        professionalData.put("ratingcount", 0);

                        userReviewDoc.set(professionalData)
                                .addOnSuccessListener(innerResult -> {
                                    Log.d("SignUp", "Professional review data created successfully");

                                    // Optionally create a "reviewspost" subcollection.
                                    CollectionReference reviewspostRef = userReviewDoc.collection("reviewspost");
                                    Map<String, Object> initialReview = new HashMap<>();
                                    initialReview.put("username", "admin");
                                    initialReview.put("reviewText", "Initial review");
                                    initialReview.put("rating", 5);

                                    reviewspostRef.document("initial_review")
                                            .set(initialReview)
                                            .addOnSuccessListener(dummyResult -> {
                                                Log.d("SignUp", "Initial review added to reviewspost");
                                            })
                                            .addOnFailureListener(e -> {
                                                Log.e("SignUp", "Error adding initial review: " + e.getMessage());
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    Log.e("SignUp", "Error creating professional review data: " + e.getMessage());
                                    Toast.makeText(sign_up.this,
                                            "Error creating professional review data",
                                            Toast.LENGTH_LONG).show();
                                });
                    }

                    // Redirect to MainActivity.
                    startActivity(new Intent(sign_up.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(sign_up.this,
                                "Firestore Error: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    /**
     * Removes commas, extra spaces, and ensures single spacing.
     * Example: "English,  Hebrew," becomes "English Hebrew".
     */
    private String normalizeLanguages(String input) {
        String noCommas = input.replace(",", " ");
        String singleSpaced = noCommas.replaceAll("\\s+", " ");
        return singleSpaced.trim();
    }

    /**
     * Normalizes an availability range entered in 24-hour format.
     * If a user enters "9 - 19", it will be converted to "09:00-19:00".
     * If a colon is already present, it will just trim extra spaces.
     */
    private String normalizeAvailability(String input) {
        if (input == null || !input.contains("-")) return input.trim();
        String[] parts = input.split("-");
        if (parts.length != 2) return input.trim();
        String start = parts[0].trim();
        String end = parts[1].trim();

        // If the start time does not contain a colon, assume it's an hour and append ":00"
        if (!start.contains(":")) {
            try {
                int hour = Integer.parseInt(start);
                start = String.format("%02d:00", hour);
            } catch (NumberFormatException e) {
                // Leave as is if parsing fails
            }
        }
        // If the end time does not contain a colon, assume it's an hour and append ":00"
        if (!end.contains(":")) {
            try {
                int hour = Integer.parseInt(end);
                end = String.format("%02d:00", hour);
            } catch (NumberFormatException e) {
                // Leave as is if parsing fails
            }
        }
        return start + "-" + end;
    }
}
