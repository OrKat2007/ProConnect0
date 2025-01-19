package com.example.proconnect;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
        Glide.with(this)
                .asBitmap()
                .load(R.drawable.home)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);

        profileImage.setOnClickListener(v -> showPictureDialog());

        username = view.findViewById(R.id.userName);


        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && currentUser.getDisplayName() != null) {
            username.setText(currentUser.getDisplayName());
        } else {
            username.setText("Unknown User");
        }

        return view;
    }

    private void showPictureDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(getContext());
        pictureDialog.setTitle("נא לבחור מאיפה להוסיף תמונה:");
        String[] pictureDialogItems = {
                "מהגלריה",
                "מהמצלמה" };
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

        if (resultCode == getActivity().RESULT_OK && data != null) {
            if (requestCode == GALLERY) {
                try {
                    Uri selectedImageUri = data.getData();
                    if (selectedImageUri != null) {
                        Bitmap selectedImage = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                        String encodedImage = encodeImageToBase64(selectedImage);
                        saveImageToFirestore(encodedImage);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == CAMERA) {
                try {
                    Bitmap photo = (Bitmap) data.getExtras().get("data");
                    if (photo != null) {
                        String encodedImage = encodeImageToBase64(photo);
                        saveImageToFirestore(encodedImage);
                    } else {
                        // Handle the case where the photo is null (optional)
                        Log.e("Camera", "No photo data received");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else {
            // Handle the case when the resultCode is not OK
            Log.e("ActivityResult", "Result not OK or data is null");
        }
    }


    private String encodeImageToBase64(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray, Base64.DEFAULT);
    }

    private void saveImageToFirestore(String encodedImage) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            firestore.collection("users").document(userId)
                    .update("profileImage", encodedImage)
                    .addOnSuccessListener(aVoid -> {
                    })
                    .addOnFailureListener(e -> {
                    });
        }
    }
}