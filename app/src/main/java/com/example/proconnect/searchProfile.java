package com.example.proconnect;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class searchProfile extends Fragment {

    // Declare professionalUid as a field (or as a local variable accessible throughout onCreateView)
    private String professionalUid = "";  // Ensure it is accessible later
    private String profileImage = "";
    private String profession ="";
    private String location = "";
    private String userName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_profile, container, false);

        // Find the "Post a review" button and set its click listener
        Button btnPostReview = view.findViewById(R.id.btnPostReview);
        btnPostReview.setOnClickListener(v -> showPostReviewDialog());

        // Retrieve the data from the arguments
        Bundle args = getArguments();
        if (args != null) {
            // Assign to the field variable, not a local variable
            professionalUid = args.getString("uid", "");
            profession = args.getString("profession", "Unknown Profession");
            location = args.getString("location", "Unknown Location");
            userName = args.getString("userName", "Unknown User");
            profileImage = args.getString("profileImage");
            int ratingSum = args.getInt("ratingSum", 0);
            int ratingCount = args.getInt("ratingCount", 0);

            // Get references to the views
            TextView userNameTextView = view.findViewById(R.id.userName);
            TextView professionTextView = view.findViewById(R.id.Proffessiontv);
            TextView locationTextView = view.findViewById(R.id.locationtv);
            RatingBar ratingBar = view.findViewById(R.id.ratingBar);
            ImageView profileImageView = view.findViewById(R.id.profileImage);

            userNameTextView.setText(userName);
            professionTextView.setText("Profession: " + profession);
            locationTextView.setText("Location: " + location);

            // Set rating if available
            if (ratingCount > 0) {
                float averageRating = (float) ratingSum / ratingCount;
                ratingBar.setRating(averageRating);
            } else {
                ratingBar.setRating(0);
            }

            // Load the profile image. Attempt Base64 decoding first; if that fails, load as URL.
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
                            .load(profileImage)
                            .transform(new CircleCrop())
                            .placeholder(R.drawable.default_profile)
                            .into(profileImageView);
                }
            } else {
                Glide.with(getContext())
                        .load(R.drawable.default_profile)
                        .transform(new CircleCrop())
                        .into(profileImageView);
            }
        }

        // In searchProfile.onCreateView(), after loading profile data:


        ImageView btnChats = view.findViewById(R.id.btnChats);
        btnChats.setOnClickListener(v -> {
            // Create an instance of Chat_Fragment
            Chat_Fragment chatFragment = new Chat_Fragment();

            // Create a new bundle for the Chat_Fragment
            Bundle chatBundle = new Bundle();

            // Pass the professional's UID (using the field variable that was set above)

            chatBundle.putString("chatPartnerUid", professionalUid);
            chatBundle.putString("chatPartnerImage", profileImage);
            chatBundle.putString("userName",userName );
            chatBundle.putString("profession",profession );
            chatBundle.putString("location",location );

            // Extract the professional's email from the original arguments
            String professionalEmail = "";
            Bundle originalArgs = getArguments();
            if (originalArgs != null) {
                professionalEmail = originalArgs.getString("email", "").toLowerCase();
            }
            Log.d("SearchProfile", "Professional email: " + professionalUid);

            // Pass it with the key "chatPartnerEmail"
            chatBundle.putString("chatPartnerEmail", professionalUid);

            // Set the new bundle to the Chat_Fragment
            chatFragment.setArguments(chatBundle);

            // Replace the current fragment with Chat_Fragment
            if (getActivity() instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) getActivity();
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

                // Replace the current fragment with ChatFragment
                transaction.replace(R.id.frame_layout, chatFragment, "CHAT_FRAGMENT");

                // Add this transaction to the back stack so the user can navigate back
                transaction.addToBackStack(null);

                // Commit the transaction
                transaction.commit();
            }

        });

        // After setting up the profile, fetch and display the reviews
        view.postDelayed(() -> fetchAndDisplayReviews(), 100);
        fetchAndDisplayReviews();

        return view;
    }

    public void showPostReviewDialog() {
        Dialog reviewDialog = new Dialog(getActivity());
        reviewDialog.setContentView(R.layout.dialog_post_review);
        reviewDialog.setCancelable(true);

        EditText etReview = reviewDialog.findViewById(R.id.etReview);
        RatingBar ratingBar = reviewDialog.findViewById(R.id.ratingBar);
        Button btnPostReview = reviewDialog.findViewById(R.id.btnPostReview);

        btnPostReview.setOnClickListener(v -> {
            String reviewText = etReview.getText().toString().trim();
            float rating = ratingBar.getRating();

            if (!reviewText.isEmpty()) {
                postReview(reviewText, rating);
                reviewDialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Please write a review", Toast.LENGTH_SHORT).show();
            }
        });

        reviewDialog.show();
    }

    // Post review without saving user name or profile URL.
    private void postReview(String reviewText, float newRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail == null || professionalUid == null) return;

        String formattedEmail = userEmail.toLowerCase().replace("@", "_").replace(".", "_");
        DocumentReference reviewDocRef = db.collection("reviews").document(professionalUid);
        DocumentReference userReviewRef = reviewDocRef.collection("reviewspost").document(formattedEmail);

        db.runTransaction(transaction -> {
            // Fetch the current professional's review document
            DocumentSnapshot reviewDoc = transaction.get(reviewDocRef);
            // Fetch the review document for this reviewer (if it exists)
            DocumentSnapshot userReviewDocSnap = transaction.get(userReviewRef);

            double ratingsum = 0;
            long ratingcount = 0;

            if (reviewDoc.exists()) {
                ratingsum = reviewDoc.getDouble("ratingsum") != null ? reviewDoc.getDouble("ratingsum") : 1;
                ratingcount = reviewDoc.getLong("ratingcount") != null ? reviewDoc.getLong("ratingcount") : 1;
            }

            float oldRating = 0;
            // If the user has not posted a review yet, this is a new review
            if (!userReviewDocSnap.exists()) {
                ratingcount++; // increment count for new review
            } else {
                oldRating = userReviewDocSnap.getDouble("rating") != null ? userReviewDocSnap.getDouble("rating").floatValue() : 0;
            }

            // Update ratingsum: subtract the old rating (if any) then add the new rating
            ratingsum = (ratingsum - oldRating) + newRating;

            // Save or update the review document for this reviewer
            ReviewModel review = new ReviewModel(userEmail, reviewText, newRating, System.currentTimeMillis());
            transaction.set(userReviewRef, review);

            // Update the professional's main review document with new rating data
            transaction.update(reviewDocRef, "ratingsum", ratingsum, "ratingcount", ratingcount);

            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Review updated!", Toast.LENGTH_SHORT).show();
            fetchAndDisplayReviews(); // Refresh UI
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error updating review: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }

    private void updateProfessionalRating(float rating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference professionalDocRef = db.collection("reviews").document(professionalUid);

        professionalDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                int ratingCount = document.contains("ratingcount") ? document.getLong("ratingcount").intValue() : 0;
                float ratingSum = document.contains("ratingsum") ? document.getDouble("ratingsum").floatValue() : 0.0f;

                ratingCount++;
                ratingSum += rating;
                float newRating = ratingSum / ratingCount;

                View view = getView();
                RatingBar ratingBar = view.findViewById(R.id.ratingBar);
                ratingBar.setRating(newRating);

                Map<String, Object> updatedData = new HashMap<>();
                updatedData.put("ratingcount", ratingCount);
                updatedData.put("ratingsum", ratingSum);
                updatedData.put("rating", newRating);

                professionalDocRef.update(updatedData)
                        .addOnFailureListener(e -> {
                            Toast.makeText(getActivity(), "Error updating rating: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    private void fetchAndDisplayReviews() {
        if (professionalUid == null || getView() == null) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference professionalDocRef = db.collection("reviews").document(professionalUid);

        professionalDocRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                DocumentSnapshot document = task.getResult();
                int ratingCount = document.contains("ratingcount") ? document.getLong("ratingcount").intValue() : 0;
                float ratingSum = document.contains("ratingsum") ? document.getDouble("ratingsum").floatValue() : 0.0f;

                float rating = ratingSum / ratingCount;

                View view = getView();
                RatingBar ratingBar = view.findViewById(R.id.ratingBar);
                ratingBar.setRating(rating);
            }
        });

        View view = getView();
        if (view == null) return;

        RecyclerView reviewsRecyclerView = view.findViewById(R.id.reviewsRecyclerView);
        if (reviewsRecyclerView == null) return;

        db.collection("reviews").document(professionalUid)
                .collection("reviewspost")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (getView() == null) return;
                    List<ReviewModel> reviewList = new ArrayList<>();

                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ReviewModel review = document.toObject(ReviewModel.class);
                        if (review != null) {
                            Log.d("ReviewData", "Reviewer Email: " + review.getReviewerEmail() +
                                    ", Text: " + review.getText() +
                                    ", Rating: " + review.getRating());
                            reviewList.add(review);
                        }
                    }

                    reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    ReviewsAdapter reviewsAdapter = new ReviewsAdapter(reviewList);
                    reviewsRecyclerView.setAdapter(reviewsAdapter);
                })
                .addOnFailureListener(e -> {
                    if (getContext() != null) {
                        Toast.makeText(getContext(), "Error fetching reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
