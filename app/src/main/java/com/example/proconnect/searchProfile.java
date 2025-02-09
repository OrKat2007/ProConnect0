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

        Bundle args = getArguments();
        if (args != null) {
            String profession = args.getString("profession");
            String location = args.getString("location");
            String userName = args.getString("userName");
            String profileImage = args.getString("profileImage");

            TextView userNameTextView = view.findViewById(R.id.userName);
            TextView professionTextView = view.findViewById(R.id.Proffessiontv);
            TextView locationTextView = view.findViewById(R.id.locationtv);
            TextView ratingTextView = view.findViewById(R.id.textView5);

            userNameTextView.setText(userName);
            professionTextView.setText("Proffession: " + profession);
            locationTextView.setText("Location: " + location);
            ratingTextView.setText("Rating: 5.0");

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
}