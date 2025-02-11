package com.example.proconnect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ReviewViewHolder> {
    private List<ReviewModel> reviewList;

    public ReviewsAdapter(List<ReviewModel> reviewList) {
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        ReviewModel review = reviewList.get(position);
        // Set the review text and rating immediately
        holder.reviewTextView.setText(review.getText());
        holder.reviewRatingBar.setRating(review.getRating());

        // Set temporary placeholder while loading user data
        holder.userNameTextView.setText("Loading...");
        holder.profileImageView.setImageResource(R.drawable.default_profile);

        // Use the reviewer's email (from the review) to fetch their details from Firestore.
        String reviewerEmail = review.getReviewerEmail();
        // Format the email the same way you did in sign_up (e.g., replace "@" and ".")
        String formattedEmail = reviewerEmail.toLowerCase().replace("@", "_").replace(".", "_");

        FirebaseFirestore.getInstance().collection("users")
                .document(formattedEmail)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // Assuming the user document stores "name" and "profileImage"
                        String name = documentSnapshot.getString("name");
                        String profileImage = documentSnapshot.getString("profileImage");

                        holder.userNameTextView.setText(name != null ? name : "Unknown");

                        if (profileImage != null && !profileImage.isEmpty()) {
                            try {
                                byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                                Glide.with(holder.itemView.getContext())
                                        .load(decodedByte)
                                        .transform(new CircleCrop())
                                        .placeholder(R.drawable.default_profile)
                                        .into(holder.profileImageView);
                            } catch (Exception e) {
                                // If Base64 decoding fails, try loading it as a URL
                                Glide.with(holder.itemView.getContext())
                                        .load(profileImage)
                                        .transform(new CircleCrop())
                                        .placeholder(R.drawable.default_profile)
                                        .into(holder.profileImageView);
                            }
                        }
                    } else {
                        holder.userNameTextView.setText("Unknown");
                    }
                })
                .addOnFailureListener(e -> holder.userNameTextView.setText("Unknown"));
    }

    @Override
    public int getItemCount() {
        return reviewList.size();
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView userNameTextView, reviewTextView;
        RatingBar reviewRatingBar;
        ImageView profileImageView;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            userNameTextView = itemView.findViewById(R.id.userNameTextView);
            reviewTextView = itemView.findViewById(R.id.reviewTextView);
            reviewRatingBar = itemView.findViewById(R.id.reviewRatingBar);
            profileImageView = itemView.findViewById(R.id.profileImageView);
        }
    }
}
