package com.example.proconnect;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Base64;
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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class searchProfile extends Fragment {

    private String professionalEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_profile, container, false);

        // Get reference to the "Post a review" button
        Button btnPostReview = view.findViewById(R.id.btnPostReview);

        // Set an OnClickListener to show the review dialog
        btnPostReview.setOnClickListener(v -> showPostReviewDialog());

        // Retrieve the data from the arguments
        Bundle args = getArguments();
        if (args != null) {
            String profession = args.getString("profession");
            String location = args.getString("location");
            String userName = args.getString("userName");
            String profileImage = args.getString("profileImage");
            int ratingSum = args.getInt("ratingSum");
            int ratingCount = args.getInt("ratingCount");
            professionalEmail = args.getString("email");

            // Set the user profile data
            TextView userNameTextView = view.findViewById(R.id.userName);
            TextView professionTextView = view.findViewById(R.id.Proffessiontv);
            TextView locationTextView = view.findViewById(R.id.locationtv);
            RatingBar ratingBar = view.findViewById(R.id.ratingBar);

            userNameTextView.setText(userName);
            professionTextView.setText("Profession: " + profession);
            locationTextView.setText("Location: " + location);

            // Calculate and set the rating
            if (ratingCount > 0) {
                float averageRating = (float) ratingSum / ratingCount;
                ratingBar.setRating(averageRating);
            } else {
                ratingBar.setRating(0);
            }

            // Set the profile image
            ImageView profileImageView = view.findViewById(R.id.profileImage);
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                    Glide.with(getContext())
                            .load(decodedByte)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.default_profile)
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

    public void showPostReviewDialog() {
        // Create the dialog
        Dialog reviewDialog = new Dialog(getActivity());
        reviewDialog.setContentView(R.layout.dialog_post_review);
        reviewDialog.setCancelable(true);

        // Get references to the EditText, RatingBar, and Post Review button
        EditText etReview = reviewDialog.findViewById(R.id.etReview);
        RatingBar ratingBar = reviewDialog.findViewById(R.id.ratingBar);
        Button btnPostReview = reviewDialog.findViewById(R.id.btnPostReview);

        // Set the OnClickListener for the Post Review button inside the dialog
        btnPostReview.setOnClickListener(v -> {
            String reviewText = etReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (!reviewText.isEmpty()) {
                // Proceed to post the review
                postReview(reviewText, rating);

                // Dismiss the dialog
                reviewDialog.dismiss();
            } else {
                // Show a message if the review text is empty
                Toast.makeText(getActivity(), "Please write a review", Toast.LENGTH_SHORT).show();
            }
        });

        // Show the dialog
        reviewDialog.show();
    }

    // The rest of your methods like postReview and updateProfessionalRating remain unchanged...


    private void postReview(String reviewText, float rating) {
        // Get the current user's email
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getActivity(), "You need to be logged in to post a review", Toast.LENGTH_SHORT).show();
            return;
        }

        String userEmail = user.getEmail();
        String formattedEmail = userEmail.replace("@", "_").replace(".", "_");

        // Create a review document for the professional in Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference reviewDocRef = db.collection("reviews")
                .document(formattedEmail) // Use the professional's formatted email as the document ID
                .collection("reviewspost") // Subcollection for reviews
                .document();

        // Prepare the review data
        Map<String, Object> reviewData = new HashMap<>();
        reviewData.put("reviewText", reviewText);
        reviewData.put("rating", rating);
        reviewData.put("timestamp", System.currentTimeMillis());

        // Add the review data to Firestore
        reviewDocRef.set(reviewData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getActivity(), "Review posted successfully", Toast.LENGTH_SHORT).show();

                    // After posting the review, update the professional's rating info
                    updateProfessionalRating(rating);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Error posting review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateProfessionalRating(float rating) {
        // Use the professional's email passed from the fragment
        String formattedEmail = professionalEmail.replace("@", "_").replace(".", "_");

        // Reference to the professional's review data in Firestore
        DocumentReference professionalDocRef = FirebaseFirestore.getInstance()
                .collection("reviews")
                .document(formattedEmail);

        professionalDocRef.get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // Get current rating count and sum, checking for null
                            int ratingCount = document.contains("ratingcount") ? document.getLong("ratingcount").intValue() : 0;
                            float ratingSum = document.contains("ratingsum") ? document.getDouble("ratingsum").floatValue() : 0.0f;

                            // Calculate new rating sum and count
                            ratingCount++;
                            ratingSum += rating;

                            // Calculate the new rating
                            float newRating = ratingSum / ratingCount;

                            // Update the professional's rating info
                            Map<String, Object> updatedData = new HashMap<>();
                            updatedData.put("ratingcount", ratingCount);
                            updatedData.put("ratingsum", ratingSum);
                            updatedData.put("rating", newRating);

                            professionalDocRef.update(updatedData)
                                    .addOnSuccessListener(aVoid -> {
                                        // Rating successfully updated
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getActivity(), "Error updating rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        }
                    }
                });
    }
}
