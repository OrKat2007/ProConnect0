package com.example.proconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

public class user_settings extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_user_settings, container, false);
        ImageView profileImage = view.findViewById(R.id.profileImage);
        Glide.with(this)
                .load(R.drawable.home)
                .apply(RequestOptions.circleCropTransform())
                .into(profileImage);

        return view;
    }
}
