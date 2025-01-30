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
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
    private TextView username;
    private FirebaseFirestore firestore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);

        firestore = FirebaseFirestore.getInstance();

        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST_CODE);
        }

        ImageButton profileImage = view.findViewById(R.id.profileImage);
        profileImage.setOnClickListener(v -> showPictureDialog());

        username = view.findViewById(R.id.userName);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            username.setText(currentUser.getDisplayName());
        } else {
            username.setText("Unknown User");
        }

        // Check and load the existing profile picture
        profileImage.setVisibility(View.INVISIBLE);
        loadProfileImage(profileImage);

        return view;
    }

    private void loadProfileImage(ImageButton profileImage) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if (user == null) {
            loadDefaultProfileImage(profileImage);
            return;
        }

        String email = user.getEmail(); // Get the user's email
        String safeEmail = email.replace("@", "_").replace(".", "_"); // Format the email

        firestore.collection("users").document(safeEmail) // Use safeEmail as document ID
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Convert Firestore document to usermodel object
                        usermodel userModel1 = documentSnapshot.toObject(usermodel.class);

                        if (userModel1 != null && userModel1.getProfileImage() != null && !userModel1.getProfileImage().isEmpty()) {
                            // Decode and load profile image
                            Bitmap bitmap = decodeBase64ToImage(userModel1.getProfileImage());
                            Glide.with(this)
                                    .load(bitmap)
                                    .apply(new RequestOptions().circleCrop().override(250, 250))
                                    .into(profileImage);

                            profileImage.setVisibility(View.VISIBLE);
                        } else {
                            loadDefaultProfileImage(profileImage);
                        }
                    } else {
                        loadDefaultProfileImage(profileImage);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("Firestore", "Failed to load profile image", e);
                    loadDefaultProfileImage(profileImage);
                });
    }

    private void loadDefaultProfileImage(ImageButton profileImage) {
        Glide.with(this)
                .load(R.drawable.home)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);
        profileImage.setVisibility(View.VISIBLE);
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("נא לבחור מאיפה להוסיף תמונה:");
        String[] pictureDialogItems = {
                "מהגלריה",
                "מהמצלמה"};
        pictureDialog.setItems(pictureDialogItems, (dialog, which) -> {
            if (which == 0) {
                choosePhotoFromGallery();
            } else if (which == 1) {
                takePhotoFromCamera();
            }
        });
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

        ImageButton profileImage = getView().findViewById(R.id.profileImage);

        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == GALLERY) {
                try {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        // Log the URI to debug if it's valid
                        Log.d("GalleryImage", "Selected image URI: " + selectedImageUri.toString());

                        // Check if the URI is valid and resolve the image
                        Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                        if (selectedImage != null) {
                            // Resize the image before encoding it
                            Bitmap resizedImage = resizeImage(selectedImage, 1024, 1024); // Resize to fit within 1024x1024

                            String encodedImage = encodeImageToBase64(resizedImage);
                            saveImageToFirestore(encodedImage);

                            Glide.with(this)
                                    .load(resizedImage)
                                    .apply(new RequestOptions()
                                            .circleCrop()
                                            .override(250, 250))
                                    .into(profileImage);
                        }
                    } else {
                        Log.e("GalleryImage", "Selected image URI is null");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA) {
                try {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null) {
                        // Resize the image before encoding it
                        Bitmap resizedImage = resizeImage(photo, 1024, 1024); // Resize to fit within 1024x1024

                        String encodedImage = encodeImageToBase64(resizedImage);
                        saveImageToFirestore(encodedImage);

                        Glide.with(this)
                                .load(resizedImage)
                                .apply(new RequestOptions()
                                        .circleCrop()
                                        .override(250, 250))
                                .into(profileImage);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream); // Compress the image to 50% quality
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveImageToFirestore(String encodedImage) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        String email = user.getEmail();
        String safeEmail = email.replace("@", "_").replace(".", "_"); // Use the same format for email

        // Update only the "profileImage" field, keeping other data intact
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("profileImage", encodedImage);

        firestore.collection("users").document(safeEmail) // Use safeEmail as document ID
                .update(updateData)  // Use update() instead of set()
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
