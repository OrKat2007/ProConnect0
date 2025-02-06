package com.example.proconnect;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;

public class searchProfile extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_profile, container, false);

        // Get the passed data from the bundle
        Bundle args = getArguments();
        if (args != null) {
            String userName = args.getString("userName");
            String profileImage = args.getString("profileImage");  // Get the profile image URL or Base64 string

            // Set the data into the view (e.g., TextView)
            TextView userNameTextView = view.findViewById(R.id.userName);
            userNameTextView.setText(userName);

            // Load the profile image using Glide
            ImageView profileImageView = view.findViewById(R.id.profileImage);  // Assuming you have an ImageView in the layout

            // Check if the profile image is not null and not empty
            if (profileImage != null && !profileImage.isEmpty()) {
                try {
                    // Decode the Base64 string into a Bitmap
                    byte[] decodedString = Base64.decode(profileImage, Base64.DEFAULT);
                    Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                    // Use Glide to load the decoded Bitmap with CircleCrop transformation
                    Glide.with(getContext())
                            .load(decodedByte)
                            .transform(new CircleCrop())  // Apply CircleCrop transformation for rounded image
                            .placeholder(R.drawable.default_profile)  // Placeholder image if the URL is null or loading
                            .error(R.drawable.default_profile)  // Error image if Glide fails
                            .into(profileImageView);
                } catch (Exception e) {
                    // If there's an error (e.g., decoding fails), load the default profile image
                    Glide.with(getContext())
                            .load(R.drawable.default_profile)
                            .transform(new CircleCrop())  // Apply CircleCrop for the default profile image
                            .into(profileImageView);
                }
            } else {
                // If the profile image is empty or null, load the default profile image
                Glide.with(getContext())
                        .load(R.drawable.default_profile)
                        .transform(new CircleCrop())  // Apply CircleCrop for the default profile image
                        .into(profileImageView);
            }
        }

        return view;
    }
}
