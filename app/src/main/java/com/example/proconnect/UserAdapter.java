package com.example.proconnect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

        // Set the rating text dynamically
        if (holder.tvRating != null) {
            if (user.getRatingCount() > 0) {
                float rating = (float) user.getRatingSum() / user.getRatingCount();
                holder.tvRating.setText("Rating: " + String.format("%.1f", rating) + " ⭐ (" + user.getRatingCount() + ")");
            } else {
                holder.tvRating.setText("No Ratings Yet");
            }
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
        TextView tvName, tvProfession, tvRating; // Add tvRating here
        ImageView ivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvProfession = itemView.findViewById(R.id.tvProfession);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvRating = itemView.findViewById(R.id.tvRating);  // Ensure tvRating is initialized
        }
    }

    // Update list when search happens
    public void updateList(List<usermodel> newList) {
        userList = newList;
        notifyDataSetChanged();
    }
}
