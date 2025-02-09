package com.example.proconnect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private List<usermodel> userList;
    private Context context;

    public UserAdapter(List<usermodel> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for each item (user_item.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view); // Return the ViewHolder with the view
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        usermodel user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvProfession.setText(user.getProfession());

        // Set the profile image using Glide with CircleCrop
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

        // Set the rating dynamically on the RatingBar
        if (holder.ratingBar != null) {
            if (user.getRatingCount() > 0) {
                float rating = user.getRatingSum() / user.getRatingCount();
                holder.ratingBar.setRating(rating);  // Set the rating on the RatingBar
            } else {
                holder.ratingBar.setRating(0);  // Set rating to 0 if no ratings
            }
        } else {
            Log.e("UserAdapter", "RatingBar is null!");
        }


        // Handle item click to navigate to the profile fragment
        holder.itemView.setOnClickListener(v -> {
            searchProfile profileFragment = new searchProfile();
            Bundle bundle = new Bundle();
            bundle.putString("uid", user.getUid());
            bundle.putString("userName", user.getName());
            bundle.putString("profession", user.getProfession());
            bundle.putString("location", user.getLocation());
            bundle.putString("profileImage", user.getProfileImage());
            bundle.putFloat("ratingSum", user.getRatingSum());  // Pass rating sum
            bundle.putInt("ratingCount", user.getRatingCount());  // Pass rating count
            profileFragment.setArguments(bundle);

            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, profileFragment); // Replace the fragment
                transaction.addToBackStack(null); // Optional: Add to backstack for navigation
                transaction.commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvProfession; // Removed tvRating
        ImageView ivProfile;
        RatingBar ratingBar;  // Added RatingBar

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvProfession = itemView.findViewById(R.id.tvProfession);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            ratingBar = itemView.findViewById(R.id.ratingBar);  // Ensure ratingBar is initialized
        }
    }

    // Update list when search happens
    public void updateList(List<usermodel> newList) {
        userList = newList;
        notifyDataSetChanged();
    }
}
