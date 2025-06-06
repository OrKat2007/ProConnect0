package com.example.proconnect.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.proconnect.R;
import com.example.proconnect.models.UserModel;
import com.example.proconnect.searchProfile;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<UserModel> userList;
    private Context context;
    private FirebaseFirestore firestore;

    public UserAdapter(List<UserModel> userList, Context context) {
        this.userList = userList;
        this.context = context;
        this.firestore = FirebaseFirestore.getInstance();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserModel user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvProfession.setText(user.getProfession());
        holder.tvAvailability.setText("Availability: " + user.getAvailability());

        // Profile image
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(user.getProfileImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(context)
                        .load(decodedByte)
                        .transform(new CircleCrop())
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(holder.ivProfile);
            } catch (Exception e) {
                Glide.with(context)
                        .load(R.drawable.default_profile)
                        .transform(new CircleCrop())
                        .into(holder.ivProfile);
            }
        } else {
            holder.ivProfile.setImageResource(R.drawable.default_profile);
        }

        // Fetch rating sum/count and set both RatingBar and RatingCount TextView
        fetchRatingFromReviews(user.getUid(), holder.ratingBar, holder.tvRatingCount);

        // Item click → profile page
        holder.itemView.setOnClickListener(v -> {
            searchProfile profileFragment = new searchProfile();
            Bundle bundle = new Bundle();
            bundle.putString("uid", user.getUid());
            bundle.putString("userName", user.getName());
            bundle.putString("profession", user.getProfession());
            bundle.putString("location", user.getLocation());
            bundle.putString("dob", user.getDob());
            bundle.putString("profileImage", user.getProfileImage());
            bundle.putInt("age", user.getAge());
            bundle.putString("languages", user.getLanguages());
            bundle.putString("availability", user.getAvailability());
            profileFragment.setArguments(bundle);

            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                FragmentTransaction ft = activity.getSupportFragmentManager().beginTransaction();
                ft.replace(R.id.frame_layout, profileFragment);
                ft.addToBackStack(null);
                ft.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    private void fetchRatingFromReviews(String userId,
                                        RatingBar ratingBar,
                                        TextView ratingCountTextView) {
        if (ratingBar == null || ratingCountTextView == null) return;

        DocumentReference reviewRef = firestore.collection("reviews").document(userId);
        reviewRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                double sum = task.getResult().getDouble("ratingsum");
                int count = task.getResult().getLong("ratingcount").intValue();
                float avg = (count > 0 ? (float)(sum / count) : 0f);
                ratingBar.setRating(avg);
                ratingCountTextView.setText("Amount of ratings: " + count);
            } else {
                ratingBar.setRating(0f);
                ratingCountTextView.setText("Amount of ratings: 0");
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(context, "Failed to fetch rating", Toast.LENGTH_SHORT).show();
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvProfession, tvAvailability, tvRatingCount;
        ImageView ivProfile;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName           = itemView.findViewById(R.id.tvName);
            tvProfession     = itemView.findViewById(R.id.tvProfession);
            ivProfile        = itemView.findViewById(R.id.ivProfile);
            ratingBar        = itemView.findViewById(R.id.ratingBar2);
            tvAvailability   = itemView.findViewById(R.id.tvAvailability);
            tvRatingCount    = itemView.findViewById(R.id.tvRatingCount);
        }
    }
}
