package com.example.proconnect;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proconnect.adapters.UserAdapter;
import com.example.proconnect.models.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class searchScreen extends Fragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserModel> userList;
    private FirebaseFirestore firestore;

    public searchScreen() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_screen, container, false);

        etSearch    = view.findViewById(R.id.etSearch);
        recyclerView= view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore   = FirebaseFirestore.getInstance();
        userList    = new ArrayList<>();
        userAdapter = new UserAdapter(userList, getContext());
        recyclerView.setAdapter(userAdapter);

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override
            public void onTextChanged(CharSequence s, int st, int b, int c) {
                searchProfessionals(s.toString());
            }
        });

        return view;
    }

    private void searchProfessionals(String query) {
        // 1) Fetch all professionals
        firestore.collection("users")
                .whereEqualTo("professional", true)
                .get()
                .addOnSuccessListener(snapshot -> {
                    userList.clear();
                    for (QueryDocumentSnapshot document : snapshot) {
                        String prof = document.getString("profession");
                        if (prof != null && prof.toLowerCase().contains(query.toLowerCase())) {
                            UserModel user = document.toObject(UserModel.class);
                            user.setProfession(prof);
                            user.setLocation(document.getString("location"));
                            user.setProfileImage(document.getString("profileImage"));

                            String uid = document.getId();
                            // 2) Fetch review data for rating & count
                            firestore.collection("reviews").document(uid)
                                    .get()
                                    .addOnSuccessListener(revDoc -> {
                                        if (revDoc.exists()) {
                                            double sum   = revDoc.getDouble("ratingsum");
                                            long count   = revDoc.getLong("ratingcount");
                                            double avg   = count > 0 ? sum/count : 0;
                                            user.setAverageRating(avg);
                                            user.setRatingCount((int) count);
                                        } else {
                                            user.setAverageRating(0);
                                            user.setRatingCount(0);
                                        }
                                        // add and sort by rating desc, then count desc
                                        userList.add(user);
                                        Collections.sort(userList, new Comparator<UserModel>() {
                                            @Override
                                            public int compare(UserModel u1, UserModel u2) {
                                                int cmp = Double.compare(u2.getAverageRating(), u1.getAverageRating());
                                                if (cmp != 0) return cmp;
                                                return Long.compare(u2.getRatingCount(), u1.getRatingCount());
                                            }
                                        });
                                        userAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(getContext(),
                                            "Error loading reviews for " + uid, Toast.LENGTH_SHORT).show());
                        }
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(getContext(),
                        "Error loading professionals", Toast.LENGTH_SHORT).show());
    }
}
