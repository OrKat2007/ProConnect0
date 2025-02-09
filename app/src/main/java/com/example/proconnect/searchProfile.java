package com.example.proconnect;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;

import java.util.HashMap;
import java.util.Map;

public class searchProfile extends Fragment {

    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_profile, container, false);

        db = FirebaseFirestore.getInstance(); // Initialize Firestore

        Bundle args = getArguments();
        if (args != null) {
            String profession = args.getString("profession");
            String location = args.getString("location");
            String userName = args.getString("userName");
            String profileImage = args.getString("profileImage");
            float ratingSum = args.getFloat("ratingSum", 0);  // Assuming ratingSum is passed
            int ratingCount = args.getInt("ratingCount", 0);  // Assuming ratingCount is passed

            TextView userNameTextView = view.findViewById(R.id.userName);
            TextView professionTextView = view.findViewById(R.id.Proffessiontv);
            TextView locationTextView = view.findViewById(R.id.locationtv);
            Button btnPostReview = view.findViewById(R.id.btnPostReview);

            btnPostReview.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Create the dialog and inflate the custom layout
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());  // Use `getContext()` or `this` for Activity context
                    View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_post_review, null);

                    // Get the views from the dialog layout
                    EditText etReview = dialogView.findViewById(R.id.etReview);
                    RatingBar ratingBar = dialogView.findViewById(R.id.ratingBar);
                    Button btnSubmitReview = dialogView.findViewById(R.id.btnSubmitReview);

                    // Set up the dialog
                    builder.setView(dialogView);
                    builder.setCancelable(true);  // Allow the dialog to be dismissed by tapping outside

                    final AlertDialog dialog = builder.create();

                    // Handle the review submission

                    btnSubmitReview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            // Get the text and rating from the dialog
                            String reviewText = etReview.getText().toString();
                            float rating = ratingBar.getRating();

                            // Check if the review text is not empty
                            if (!reviewText.isEmpty()) {
                                // Get the user's email or ID
                                String userId = FirebaseAuth.getInstance().getCurrentUser().getEmail();

                                if (userId != null) {
                                    // Save the review to Firestore
                                    saveReview(userId, reviewText, rating);

                                    // Show a toast with the review and rating
                                    Toast.makeText(getContext(), "Review: " + reviewText + "\nRating: " + rating, Toast.LENGTH_LONG).show();

                                    // Dismiss the dialog after submitting the review
                                    dialog.dismiss();
                                } else {
                                    // Handle the case if the user is not authenticated
                                    Toast.makeText(getContext(), "User not authenticated. Please log in.", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                // If the review text is empty, show a message
                                Toast.makeText(getContext(), "Please write a review.", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

// Show the dialog
                    dialog.show();

                }
            });

            // Replace the rating TextView with the RatingBar
            RatingBar ratingBar = view.findViewById(R.id.ratingBar);

            userNameTextView.setText(userName);
            professionTextView.setText("Profession: " + profession);
            locationTextView.setText("Location: " + location);

            // Calculate and set the rating
            if (ratingCount > 0) {
                float rating = ratingSum / ratingCount;
                ratingBar.setRating(rating);  // Set the rating on the RatingBar
            } else {
                ratingBar.setRating(0);  // Set rating to 0 if no ratings
            }

            ImageView profileImageView = view.findViewById(R.id.profileImage);
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Glide.with(getContext())
                            .load(decodedByte)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.default_profile)
                            .error(R.drawable.default_profile)
                            .into(profileImageView);
                } catch (Exception e) {
                    Glide.with(getContext())
                            .load(R.drawable.default_profile)
                            .transform(new CircleCrop())
                            .into(profileImageView);
                }
            } else {
                Glide.with(getContext())
                        .load(R.drawable.default_profile)
                        .transform(new CircleCrop())
                        .into(profileImageView);
            }
        }

        return view;
    }

    public void saveReview(String userId, String reviewText, float rating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Create a new PostReviewModel
        ReviewModel review = new ReviewModel(reviewText, userId, rating);

        db.collection("reviews")
                .document(userId) // Document ID is user's email or unique ID
                .collection("userReviews") // Subcollection for user reviews
                .add(review) // Save review model object
                .addOnSuccessListener(documentReference -> {
                    Log.d("Review", "Review saved successfully!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Review", "Error saving review", e);
                });
    }

}
