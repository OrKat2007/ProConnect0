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
    private FragmentTransaction fragmentTransaction;

    public UserAdapter(List<usermodel> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        usermodel user = userList.get(position);
        holder.tvName.setText(user.getName());
        holder.tvProfession.setText(user.getProfession());

        // Load profile image using Glide and decode Base64 if needed
        if (user.getProfileImage() != null && !user.getProfileImage().isEmpty()) {
            try {
                byte[] decodedString = Base64.decode(user.getProfileImage(), Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                Glide.with(context)
                        .load(decodedByte)
                        .transform(new CircleCrop())  // Apply CircleCrop transformation for rounded image
                        .placeholder(R.drawable.default_profile)
                        .error(R.drawable.default_profile)
                        .into(holder.ivProfile);
            } catch (Exception e) {
                Glide.with(context)
                        .load(R.drawable.default_profile)
                        .transform(new CircleCrop())  // Apply CircleCrop for the default profile image
                        .into(holder.ivProfile);
            }
        } else {
            holder.ivProfile.setImageResource(R.drawable.default_profile);
        }

        // Handle item click to go to searchProfile fragment
        holder.itemView.setOnClickListener(view -> {
            // Assuming you're in an activity and using FragmentManager to replace fragments
            Fragment fragment = new searchProfile();  // Create an instance of searchProfile fragment

            // Pass user data to searchProfile fragment (you can add more data here)
            Bundle bundle = new Bundle();
            bundle.putString("userName", user.getName());
            bundle.putString("profileImage", user.getProfileImage());  // You can pass the profile image URL
            fragment.setArguments(bundle);

            // Load the searchProfile fragment
            if (context instanceof AppCompatActivity) {
                AppCompatActivity activity = (AppCompatActivity) context;
                FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.frame_layout, fragment);  // Replace the current fragment with searchProfile
                transaction.addToBackStack(null);  // Optional: Add to backstack so the user can navigate back
                transaction.commit();
            }
        });
    }


    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvProfession;
        ImageView ivProfile;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvName);
            tvProfession = itemView.findViewById(R.id.tvProfession);
            ivProfile = itemView.findViewById(R.id.ivProfile);
        }
    }

    // Update list when search happens
    public void updateList(List<usermodel> newList) {
        userList = newList;
        notifyDataSetChanged();
    }
}
