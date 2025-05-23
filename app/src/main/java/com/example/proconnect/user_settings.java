package com.example.proconnect;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.BatteryManager;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.proconnect.models.UserModel;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class user_settings extends Fragment {

    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY = 1, CAMERA = 2;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    private ImageButton profileImage;
    private TextView userName, textViewAge, textViewRating, textViewProfession;
    private TextView tvLocation, tvAvailability, tvshowLocation;
    private Spinner spinnerLanguage;
    private EditText etAvailability;
    private Button btnSave, btnDelete;

    private FirebaseFirestore firestore;
    private FusedLocationProviderClient fusedLocationClient;

    private final String[] languagesArray = {"Hebrew", "English", "Russian", "Spanish", "French", "German", "Chinese", "Italian", "Portuguese", "Japanese"};

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        firestore = FirebaseFirestore.getInstance();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getEmail().toLowerCase().replace("@", "_").replace(".", "_");

        profileImage = view.findViewById(R.id.profileImage);
        btnSave = view.findViewById(R.id.btnSave);
        btnDelete = view.findViewById(R.id.btnDelete);
        Button logout = view.findViewById(R.id.btnLogOut);
        Button btnUpdateLocation = view.findViewById(R.id.btnUpdateLocation);

        userName = view.findViewById(R.id.userName);
        textViewAge = view.findViewById(R.id.textViewAge);
        textViewRating = view.findViewById(R.id.textViewRating);
        textViewProfession = view.findViewById(R.id.textViewProfession);
        tvLocation = view.findViewById(R.id.tvLocation);
        tvAvailability = view.findViewById(R.id.tvAvailability);
        tvshowLocation = view.findViewById(R.id.tvshowLocation);

        spinnerLanguage = view.findViewById(R.id.textViewLanguages);
        etAvailability = view.findViewById(R.id.editTextAvailability);

        FirebaseUser currentUser = auth.getCurrentUser();
        userName.setText(currentUser != null && currentUser.getDisplayName() != null ? currentUser.getDisplayName() : "Unknown User");

        profileImage.setOnClickListener(v -> showPictureDialog());
        profileImage.setVisibility(View.INVISIBLE);
        loadProfileImage(profileImage);

        logout.setOnClickListener(v -> {
            auth.signOut();
            startActivity(new Intent(getActivity(), login_screen.class)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
            getActivity().finish();
        });

        btnSave.setOnClickListener(v -> saveUpdatedUserFields());
        btnDelete.setOnClickListener(v -> showDeleteUserDialog(userId));
        setupLanguageSpinner();

        btnUpdateLocation.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            } else {
                fetchAndUpdateLocation();
            }
        });

        firestore.collection("users").document(userId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                textViewProfession.setText("Profession: " + document.getString("profession"));
                String dob = document.getString("dob");
                textViewAge.setText("Age: " + (dob != null ? calculateAge(dob) : "N/A"));
                tvshowLocation.setText(document.getString("location"));

                String languages = document.getString("languages");
                if (!TextUtils.isEmpty(languages)) {
                    spinnerLanguage.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, new String[]{languages}));
                }

                boolean isPro = Boolean.TRUE.equals(document.getBoolean("professional"));
                if (isPro) {
                    textViewProfession.setVisibility(View.VISIBLE);
                    etAvailability.setVisibility(View.VISIBLE);
                    textViewRating.setVisibility(View.VISIBLE);
                    spinnerLanguage.setVisibility(View.VISIBLE);
                    tvLocation.setVisibility(View.VISIBLE);
                    tvAvailability.setVisibility(View.VISIBLE);
                    tvshowLocation.setVisibility(View.VISIBLE);
                    etAvailability.setText(document.getString("availability"));

                    firestore.collection("reviews").document(userId).get().addOnSuccessListener(reviewDoc -> {
                        if (reviewDoc.exists()) {
                            double ratingsum = reviewDoc.getDouble("ratingsum");
                            int ratingcount = reviewDoc.getLong("ratingcount").intValue();
                            double rating = ratingsum / ratingcount;
                            textViewRating.setText(rating == 0 || Double.isNaN(rating) ? "No Rating" : "Rating: " + rating);
                        }
                    });
                }
            }
        });

        return view;
    }

    private void fetchAndUpdateLocation() {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        Geocoder geocoder = new Geocoder(getContext(), Locale.ENGLISH);
                        try {
                            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!addresses.isEmpty()) {
                                String city = addresses.get(0).getLocality();
                                tvshowLocation.setText(city);
                                Toast.makeText(getContext(), "Location updated: " + city, Toast.LENGTH_SHORT).show();
                            }
                        } catch (IOException e) {
                            Toast.makeText(getContext(), "Geocoder error", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "Location is null", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Location failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void saveUpdatedUserFields() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String email = user.getEmail();
        String safeEmail = email.toLowerCase().replace("@", "_").replace(".", "_");

        String newLanguages = spinnerLanguage.getSelectedItem().toString().trim();
        String newLocation = tvshowLocation.getText().toString();
        String newAvailability = etAvailability.getText().toString().trim();

        newLanguages = normalizeLanguages(newLanguages);
        newAvailability = normalizeAvailability(newAvailability);

        Map<String, Object> updateData = new HashMap<>();
        updateData.put("languages", newLanguages);
        updateData.put("location", newLocation);
        updateData.put("availability", newAvailability);

        firestore.collection("users").document(safeEmail)
                .update(updateData)
                .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Settings updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update settings: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }


    private int getBatteryPercentage(Context context) {
        Intent batteryStatus = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;
        if (level == -1 || scale == -1) {
            return 50; // fallback
        }
        return (int) ((level / (float) scale) * 100);
    }

    private void setupLanguageSpinner() {
        ArrayAdapter<String> defaultAdapter = new ArrayAdapter<>(getContext(),
                android.R.layout.simple_spinner_item, new String[]{"Select Languages"});
        defaultAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLanguage.setAdapter(defaultAdapter);

        spinnerLanguage.setOnTouchListener((v, event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                showLanguageMultiSelectDialog();
            }
            return true;
        });
    }

    private void showLanguageMultiSelectDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Select Languages");

        String currentSelection = spinnerLanguage.getSelectedItem().toString();
        boolean[] checkedItems = new boolean[languagesArray.length];

        if (!currentSelection.equals("Select Languages")) {
            String[] selectedLanguagesArray = currentSelection.split(",\\s*");
            for (int i = 0; i < languagesArray.length; i++) {
                for (String lang : selectedLanguagesArray) {
                    if (languagesArray[i].equalsIgnoreCase(lang)) {
                        checkedItems[i] = true;
                        break;
                    }
                }
            }
        }

        builder.setMultiChoiceItems(languagesArray, checkedItems, (dialog, which, isChecked) -> {
            checkedItems[which] = isChecked;
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            StringBuilder selectedLanguages = new StringBuilder();
            for (int i = 0; i < languagesArray.length; i++) {
                if (checkedItems[i]) {
                    if (selectedLanguages.length() > 0) {
                        selectedLanguages.append(", ");
                    }
                    selectedLanguages.append(languagesArray[i]);
                }
            }
            String displayText = selectedLanguages.length() > 0 ? selectedLanguages.toString() : "Select Languages";

            ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(),
                    android.R.layout.simple_spinner_item, new String[]{displayText});
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerLanguage.setAdapter(adapter);
        });

        builder.setNegativeButton("Cancel", null);
        builder.create().show();
    }

    private int calculateAge(String dobString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        try {
            Date dob = sdf.parse(dobString);
            Calendar dobCal = Calendar.getInstance();
            dobCal.setTime(dob);
            Calendar today = Calendar.getInstance();
            int age = today.get(Calendar.YEAR) - dobCal.get(Calendar.YEAR);
            if (today.get(Calendar.DAY_OF_YEAR) < dobCal.get(Calendar.DAY_OF_YEAR)) {
                age--;
            }
            return age;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void loadProfileImage(ImageButton profileImage) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            profileImage.setVisibility(View.VISIBLE);
            return;
        }
        String email = user.getEmail();
        String safeEmail = email.toLowerCase().replace("@", "_").replace(".", "_");
        firestore.collection("users").document(safeEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        UserModel userModel1 = documentSnapshot.toObject(UserModel.class);
                        if (userModel1 != null && userModel1.getProfileImage() != null && !userModel1.getProfileImage().isEmpty()) {
                            Bitmap bitmap = decodeBase64ToImage(userModel1.getProfileImage());
                            profileImage.setImageBitmap(bitmap);
                            profileImage.setVisibility(View.VISIBLE);
                        } else {
                            profileImage.setVisibility(View.VISIBLE);
                        }
                    } else {
                        profileImage.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to load profile image", e);
                    profileImage.setVisibility(View.VISIBLE);
                });
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("נא לבחור מאיפה להוסיף תמונה:");
        String[] pictureDialogItems = {"מהגלריה", "מהמצלמה"};
        pictureDialog.setItems(pictureDialogItems, (dialog, which) -> {
            if (which == 0) {
                choosePhotoFromGallery();
            } else if (which == 1) {
                takePhotoFromCamera();
            }
        });
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, GALLERY);
    }

    private void takePhotoFromCamera() {
        int batteryLevel = getBatteryPercentage(requireContext());
        if (batteryLevel < 10) {
            Toast.makeText(getContext(), "Battery too low to open camera", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, CAMERA);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        profileImage = getView().findViewById(R.id.profileImage);
        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == GALLERY) {
                try {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                        Bitmap resizedImage = resizeImage(selectedImage, 1024, 1024);
                        String encodedImage = encodeImageToBase64(resizedImage);
                        saveImageToFirestore(encodedImage);
                        profileImage.setImageBitmap(resizedImage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA) {
                try {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null) {
                        Bitmap resizedImage = resizeImage(photo, 1024, 1024);
                        String encodedImage = encodeImageToBase64(resizedImage);
                        saveImageToFirestore(encodedImage);
                        profileImage.setImageBitmap(resizedImage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveImageToFirestore(String encodedImage) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;
        String email = user.getEmail();
        String safeEmail = email.toLowerCase().replace("@", "_").replace(".", "_");
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("profileImage", encodedImage);
        firestore.collection("users").document(safeEmail)
                .update(updateData)
                .addOnSuccessListener(aVoid -> Log.d("Firestore", "Profile image updated successfully"))
                .addOnFailureListener(e -> Log.e("Firestore", "Failed to update profile image", e));
    }

    private Bitmap decodeBase64ToImage(String encodedImage) {
        byte[] decodedBytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
    }

    private Bitmap resizeImage(Bitmap originalImage, int maxWidth, int maxHeight) {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();
        float aspectRatio = (float) width / height;
        if (width > height) {
            width = maxWidth;
            height = Math.round(width / aspectRatio);
        } else {
            height = maxHeight;
            width = Math.round(height * aspectRatio);
        }
        return Bitmap.createScaledBitmap(originalImage, width, height, false);
    }

    // Normalize languages: remove commas and extra spaces.
    private String normalizeLanguages(String input) {
        String noCommas = input.replace(",", " ");
        String singleSpaced = noCommas.replaceAll("\\s+", " ");
        return singleSpaced.trim();
    }

    // Normalize availability: convert "9 - 19" to "09:00-19:00"
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

    // Show a custom dialog to confirm user deletion.
    private void showDeleteUserDialog(String userId) {
        // Inflate the custom dialog layout.
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View dialogView = inflater.inflate(R.layout.custom_dialog_delete, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setView(dialogView);

        // Obtain references to the views in the custom layout.
        TextView tvDialogTitle = dialogView.findViewById(R.id.tvDialogTitle);
        TextView tvDialogMessage = dialogView.findViewById(R.id.tvDialogMessage);
        Button btnDialogCancel = dialogView.findViewById(R.id.btnDialogCancel);
        Button btnDialogDelete = dialogView.findViewById(R.id.btnDialogDelete);

        // Create the dialog.
        AlertDialog dialog = builder.create();

        // Set click listeners for the buttons.
        btnDialogCancel.setOnClickListener(v -> dialog.dismiss());
        btnDialogDelete.setOnClickListener(v -> {
            // Call deleteUser after confirmation.
            deleteUser(userId);
            dialog.dismiss();
        });

        dialog.show();
    }

    // Delete the user's document from Firestore and sign them out.
    private void deleteUser(String userId) {
        firestore.collection("users").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "User deleted successfully", Toast.LENGTH_SHORT).show();
                    FirebaseAuth.getInstance().signOut();
                    Intent intent = new Intent(getActivity(), login_screen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to delete user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
