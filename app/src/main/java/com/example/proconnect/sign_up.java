package com.example.proconnect;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import com.example.proconnect.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class sign_up extends AppCompatActivity implements View.OnClickListener {

    private EditText etusername, etpassword, etEmail;
    private Button back, signup, probtn, userbtn, ageButton;
    private TextView birthdateTextView;
    private Spinner spinnerLanguage;  // Use this instead of an EditText

    private boolean isPro = false;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private String selectedDob = "";

    private String proProfession = "";
    private String proLocation = "";
    private String proAvailability = "";

    // Define a list of popular languages
    private final String[] languagesArray = {"Hebrew", "English", "Russian", "Spanish", "French", "German", "Chinese", "Italian", "Portuguese", "Japanese"};
    // Keep track of selected items
    private boolean[] selectedLanguages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        etusername = findViewById(R.id.etUserName);
        etpassword = findViewById(R.id.etPassWord);
        etEmail = findViewById(R.id.etEmail);
        spinnerLanguage = findViewById(R.id.spinnerLanguage);
        ageButton = findViewById(R.id.etAgebtn);
        birthdateTextView = findViewById(R.id.birthdatetv);
        probtn = findViewById(R.id.btnPro);
        userbtn = findViewById(R.id.btnUser);
        signup = findViewById(R.id.btnRealSignUp);
        back = findViewById(R.id.btnBack);

        // Initialize the selectedLanguages array
        selectedLanguages = new boolean[languagesArray.length];

        // Set a default adapter for the spinner
        ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{"Select Languages"});
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(defaultAdapter);

        // Override spinner touch to show multi-choice dialog
        spinnerLanguage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showLanguageMultiSelectDialog();
            }
            return true; // consume the touch event
        });

        signup.setOnClickListener(this);
        back.setOnClickListener(this);
        probtn.setOnClickListener(this);
        userbtn.setOnClickListener(this);
        ageButton.setOnClickListener(this);
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
            showProfessionalDialog();
        }
        if (v == userbtn) {
            isPro = false;
            updateButtonColors();
            proProfession = "";
            proLocation = "";
            proAvailability = "";
        }
        if (v == signup) {
            registerUser();
        }
        if (v == ageButton) {
            showDatePickerDialog();
        }
    }
    private void updateButtonColors() {
        if (isPro) {
            probtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF003FFF")));//blue
            userbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));//cyan
        } else {
            userbtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#FF003FFF")));//blue
            probtn.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#2196F3")));//cyan
        }
    }

    private void showDatePickerDialog() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this, (view, selectedYear, selectedMonth, selectedDay) -> {
            selectedDob = String.format("%04d-%02d-%02d", selectedYear, selectedMonth + 1, selectedDay);
            birthdateTextView.setText("Birthdate: " + selectedDob);
        }, year, month, day);

        // Set maximum selectable date to December 31, 2009 (dates before 2010)
        Calendar maxDate = Calendar.getInstance();
        maxDate.set(2009, Calendar.DECEMBER, 31);
        datePickerDialog.getDatePicker().setMaxDate(maxDate.getTimeInMillis());

        datePickerDialog.show();
    }



    private void showProfessionalDialog() {
        // Your existing code to show the professional details dialog...
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_professional_details, null);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        AlertDialog dialog = builder.create();

        final EditText etDialogProfession = dialogView.findViewById(R.id.etDialogProfession);
        final EditText etDialogLocation = dialogView.findViewById(R.id.etDialogLocation);
        final EditText etDialogAvailability = dialogView.findViewById(R.id.etDialogAvailability);
        Button btnDialogOk = dialogView.findViewById(R.id.btnDialogOk);

        btnDialogOk.setOnClickListener(view -> {
            proProfession = etDialogProfession.getText().toString().trim();
            proLocation = etDialogLocation.getText().toString().trim();
            String rawAvailability = etDialogAvailability.getText().toString().trim();
            proAvailability = normalizeAvailability(rawAvailability);
            if (proProfession.isEmpty() || proLocation.isEmpty() || proAvailability.isEmpty()) {
                Toast.makeText(sign_up.this, "Please fill all professional fields", Toast.LENGTH_SHORT).show();
            } else {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    // Show a multi-choice dialog for language selection
    private void showLanguageMultiSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Languages");

        builder.setMultiChoiceItems(languagesArray, selectedLanguages, (dialog, which, isChecked) -> {
            // Update the current focused item's checked status
            selectedLanguages[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // Build the selected languages string
            StringBuilder selectedItems = new StringBuilder();
            for (int i = 0; i < languagesArray.length; i++) {
                if (selectedLanguages[i]) {
                    if (selectedItems.length() > 0) {
                        selectedItems.append(", ");
                    }
                    selectedItems.append(languagesArray[i]);
                }
            }
            // Update the spinner adapter to display the chosen languages
            String displayText = selectedItems.length() > 0 ? selectedItems.toString() : "Select Languages";
            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new String[]{displayText});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLanguage.setAdapter(adapter);
        });
        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private void registerUser() {
        String name = etusername.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String password = etpassword.getText().toString().trim();
        // Retrieve the selected languages from the spinner's adapter
        String normalizedLanguages = spinnerLanguage.getSelectedItem() != null ? spinnerLanguage.getSelectedItem().toString() : "";

        if (selectedDob.isEmpty()) {
            Toast.makeText(this, "Please select your birthdate", Toast.LENGTH_SHORT).show();
            return;
        }

        if (name.isEmpty() || email.isEmpty() || password.isEmpty() || normalizedLanguages.isEmpty() || normalizedLanguages.equals("Select Languages")) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (isPro && (proProfession.isEmpty() || proLocation.isEmpty() || proAvailability.isEmpty())) {
            Toast.makeText(this, "Please fill professional details", Toast.LENGTH_SHORT).show();
            showProfessionalDialog();
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
                                    saveUserToFirestore(user.getEmail(), name, email, normalizedLanguages);
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

    private void saveUserToFirestore(String safeEmail, String name, String email, String languages) {
        String formattedEmail = email.toLowerCase().replace("@", "_").replace(".", "_");
        String finalProfession = isPro ? proProfession : "None";
        String finalLocation = isPro ? proLocation : "None";
        String finalAvailability = isPro ? proAvailability : "None";

        Map<String, Object> userData = new HashMap<>();
        userData.put("uid", formattedEmail);
        userData.put("name", name);
        userData.put("email", email);
        userData.put("professional", isPro);
        userData.put("profession", finalProfession);
        userData.put("location", finalLocation);
        userData.put("languages", languages);
        userData.put("availability", finalAvailability);
        userData.put("dob", selectedDob);

        firestore.collection("users").document(formattedEmail)
                .set(userData)
                .addOnSuccessListener(result -> {
                    Toast.makeText(sign_up.this, "User saved to Firestore", Toast.LENGTH_SHORT).show();

                    if (isPro) {
                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                        DocumentReference userReviewDoc = db.collection("reviews").document(formattedEmail);

                        Map<String, Object> professionalData = new HashMap<>();
                        professionalData.put("rating", 0f);
                        professionalData.put("ratingsum", 0);
                        professionalData.put("ratingcount", 0);

                        userReviewDoc.set(professionalData)
                                .addOnSuccessListener(innerResult -> {

                                    // Optionally create a "reviewspost" subcollection.
                                    CollectionReference reviewspostRef = userReviewDoc.collection("reviewspost");
                                    Map<String, Object> initialReview = new HashMap<>();
                                    initialReview.put("username", "admin");
                                    initialReview.put("reviewText", "Initial review");
                                    initialReview.put("rating", 5);

                                    reviewspostRef.document("initial_review")
                                            .set(initialReview);
                                });
                    }
                    startActivity(new Intent(sign_up.this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> Toast.makeText(sign_up.this, "Firestore Error: " + e.getMessage(), Toast.LENGTH_LONG).show());

    }

    private String normalizeAvailability(String input) {
        if (input == null || !input.contains("-")) return input.trim();
        String[] parts = input.split("-");
        if (parts.length != 2) return input.trim();
        String start = parts[0].trim();
        String end = parts[1].trim();
        if (!start.contains(":")) {
            try {
                int hour = Integer.parseInt(start);
                start = String.format("%02d:00", hour);
            } catch (NumberFormatException e) { }
        }
        if (!end.contains(":")) {
            try {
                int hour = Integer.parseInt(end);
                end = String.format("%02d:00", hour);
            } catch (NumberFormatException e) { }
        }
        return start + "-" + end;
    }
}
