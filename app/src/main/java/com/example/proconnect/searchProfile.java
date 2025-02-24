package com.example.proconnect;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
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
import com.example.proconnect.adapters.ReviewsAdapter;
import com.example.proconnect.models.ReviewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class searchProfile extends Fragment {

    private String professionalUid = "";
    private String profileImage = "";
    private String profession = "";
    private String location = "";
    private String userName = "";
    // Buttons for review and chat
    private Button btnPostReview;
    private ImageView btnChats;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_profile, container, false);

        // Retrieve view references
        btnPostReview = view.findViewById(R.id.btnPostReview);
        btnChats = view.findViewById(R.id.btnChats);
        TextView userNameTextView = view.findViewById(R.id.userName);
        TextView professionTextView = view.findViewById(R.id.Proffessiontv);
        TextView locationTextView = view.findViewById(R.id.locationtv);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        ImageView profileImageView = view.findViewById(R.id.profileImage);

        // Retrieve data from arguments
        Bundle args = getArguments();
        if (args != null) {
            professionalUid = args.getString("uid", "");
            profession = args.getString("profession", "Unknown Profession");
            location = args.getString("location", "Unknown Location");
            userName = args.getString("userName", "Unknown User");
            profileImage = args.getString("profileImage", "");
            int ratingSum = args.getInt("ratingSum", 0);
            int ratingCount = args.getInt("ratingCount", 0);

            userNameTextView.setText(userName);
            professionTextView.setText("Profession: " + profession);
            locationTextView.setText("Location: " + location);

            if (ratingCount > 0) {
                float averageRating = (float) ratingSum / ratingCount;
                ratingBar.setRating(averageRating);
            } else {
                ratingBar.setRating(0);
            }

            // Load profile image (using Base64 decoding or as URL)
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    byte[] decodedBytes = Base64.decode(profileImage, Base64.DEFAULT);
                    Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                    Glide.with(getContext())
                            .load(bitmap)
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

        // Prevent self-review or self-chat by comparing current user UID to professional UID.
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null && professionalUid != null) {
            if (currentUser.getUid().equals(professionalUid)) {
                btnPostReview.setVisibility(View.GONE);
                btnChats.setVisibility(View.GONE);
            } else {
                btnPostReview.setVisibility(View.VISIBLE);
                btnChats.setVisibility(View.VISIBLE);
                btnPostReview.setOnClickListener(v -> showPostReviewDialog());
                btnChats.setOnClickListener(v -> launchChatFragment());
            }
        }

        // Load reviews
        view.postDelayed(() -> fetchAndDisplayReviews(), 0);
        fetchAndDisplayReviews();

        return view;
    }

    public void showPostReviewDialog() {
        Dialog reviewDialog = new Dialog(getActivity());
        reviewDialog.setContentView(R.layout.dialog_post_review);
        reviewDialog.setCancelable(true);

        EditText etReview = reviewDialog.findViewById(R.id.etReview);
        RatingBar ratingBar = reviewDialog.findViewById(R.id.ratingBar);
        Button btnDialogPostReview = reviewDialog.findViewById(R.id.btnPostReview);

        btnDialogPostReview.setOnClickListener(v -> {
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

    private void launchChatFragment() {
        Chat_Fragment chatFragment = new Chat_Fragment();
        Bundle chatBundle = new Bundle();
        chatBundle.putString("chatPartnerUid", professionalUid);
        chatBundle.putString("chatPartnerImage", profileImage);
        chatBundle.putString("userName", userName);
        chatBundle.putString("profession", profession);
        chatBundle.putString("location", location);
        chatFragment.setArguments(chatBundle);

        if (getActivity() != null) {
            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.frame_layout, chatFragment, "CHAT_FRAGMENT");
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    /**
     * Posts a review with the current timestamp.
     */
    private void postReview(String reviewText, float newRating) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userEmail = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if (userEmail == null || professionalUid == null) return;

        String formattedEmail = userEmail.toLowerCase().replace("@", "_").replace(".", "_");
        DocumentReference reviewDocRef = db.collection("reviews").document(professionalUid);
        DocumentReference userReviewRef = reviewDocRef.collection("reviewspost").document(formattedEmail);

        db.runTransaction(transaction -> {
            DocumentSnapshot reviewDoc = transaction.get(reviewDocRef);
            DocumentSnapshot userReviewDocSnap = transaction.get(userReviewRef);

            double ratingsum = 0;
            long ratingcount = 0;

            if (reviewDoc.exists()) {
                ratingsum = reviewDoc.getDouble("ratingsum") != null ? reviewDoc.getDouble("ratingsum") : 1;
                ratingcount = reviewDoc.getLong("ratingcount") != null ? reviewDoc.getLong("ratingcount") : 1;
            }

            float oldRating = 0;
            if (!userReviewDocSnap.exists()) {
                ratingcount++;
            } else {
                oldRating = userReviewDocSnap.getDouble("rating") != null ? userReviewDocSnap.getDouble("rating").floatValue() : 0;
            }

            ratingsum = (ratingsum - oldRating) + newRating;
            // Create a new review with a timestamp (using System.currentTimeMillis())
            ReviewModel review = new ReviewModel(userEmail, reviewText, newRating, System.currentTimeMillis());
            transaction.set(userReviewRef, review);
            transaction.update(reviewDocRef, "ratingsum", ratingsum, "ratingcount", ratingcount);
            return null;
        }).addOnSuccessListener(aVoid -> {
            Toast.makeText(getContext(), "Review updated!", Toast.LENGTH_SHORT).show();
            fetchAndDisplayReviews();
        }).addOnFailureListener(e ->
                Toast.makeText(getContext(), "Error updating review: " + e.getMessage(), Toast.LENGTH_SHORT).show()
        );
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
                float rating = ratingCount > 0 ? ratingSum / ratingCount : 0;
                RatingBar ratingBar = getView().findViewById(R.id.ratingBar);
                ratingBar.setRating(rating);
            }
        });

        RecyclerView reviewsRecyclerView = getView().findViewById(R.id.reviewsRecyclerView);
        if (reviewsRecyclerView == null) return;

        db.collection("reviews").document(professionalUid)
                .collection("reviewspost")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<ReviewModel> reviewList = new ArrayList<>();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        ReviewModel review = document.toObject(ReviewModel.class);
                        if (review != null) {
                            Log.d("ReviewData", "Reviewer Email: " + review.getReviewerEmail() +
                                    ", Text: " + review.getText() +
                                    ", Rating: " + review.getRating() +
                                    ", Timestamp: " + review.getTimestamp());
                            reviewList.add(review);
                        }
                    }
                    reviewsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                    ReviewsAdapter reviewsAdapter = new ReviewsAdapter(reviewList);
                    reviewsRecyclerView.setAdapter(reviewsAdapter);
                }).addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error fetching reviews: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
