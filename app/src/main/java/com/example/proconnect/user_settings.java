package com.example.proconnect;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.proconnect.models.UserModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class user_settings extends Fragment {
    private static final int CAMERA_PERMISSION_REQUEST_CODE = 100;
    private static final int GALLERY = 1, CAMERA = 2;
    ImageButton profileImage;
    private TextView username;
    private Button logout;
    private FirebaseFirestore firestore;
    private TextView userName, textViewAge, textViewLocation, textViewLanguages, textViewAvailability, textViewRating;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        firestore = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String userId = auth.getCurrentUser().getEmail().replace("@", "_").replace(".", "_");

        profileImage = view.findViewById(R.id.profileImage);
        Button logout = view.findViewById(R.id.btnLogOut);
        userName = view.findViewById(R.id.userName);
        textViewAge = view.findViewById(R.id.textViewAge);
        textViewLocation = view.findViewById(R.id.textViewLocation);
        textViewLanguages = view.findViewById(R.id.textViewLanguages);
        textViewAvailability = view.findViewById(R.id.textViewAvailability);
        textViewRating = view.findViewById(R.id.textViewRating);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            userName.setText(currentUser.getDisplayName());
        } else {
            userName.setText("Unknown User");
        }

        profileImage.setOnClickListener(v -> {
            showPictureDialog();
        });

        profileImage.setVisibility(View.INVISIBLE);
        loadProfileImage(profileImage);

        logout.setOnClickListener(v -> {
            auth.signOut();
            Intent intent = new Intent(getActivity(), login_screen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();
        });

        firestore.collection("users").document(userId).get().addOnSuccessListener(document -> {
            if (document.exists()) {
                textViewAge.setText("Age: " + document.getLong("age"));
                textViewLocation.setText("Location: " + document.getString("location"));
                textViewLanguages.setText("Languages: " + document.getString("languages"));

                boolean isPro = document.getBoolean("professional") != null && document.getBoolean("professional");
                if (isPro) {
                    textViewAvailability.setVisibility(View.VISIBLE);
                    textViewRating.setVisibility(View.VISIBLE);
                    textViewLocation.setVisibility(View.VISIBLE);
                    textViewAvailability.setText("Availability: " + document.getString("availability"));

                    firestore.collection("reviews").document(userId).get().addOnSuccessListener(reviewDoc -> {
                        if (reviewDoc.exists()) {
                            double rating = reviewDoc.getDouble("rating");
                            textViewRating.setText("Rating: " + rating);
                        }
                    });
                }
            }
        });

        return view;
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
        String safeEmail = email.replace("@", "_").replace(".", "_");

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
}
