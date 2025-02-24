package com.example.proconnect;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proconnect.adapters.UserAdapter;
import com.example.proconnect.models.UserModel;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
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

        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();
        userList = new ArrayList<>();
        userAdapter = new UserAdapter(userList, getContext());
        recyclerView.setAdapter(userAdapter);

        // Search in real-time
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProfessionals(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        return view;
    }

    private void searchProfessionals(String query) {
        firestore.collection("users")
                .whereEqualTo("professional", true) // Only fetch professionals
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        userList.clear();

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String profession = document.getString("profession");
                            if (profession != null && profession.toLowerCase().contains(query.toLowerCase())) {
                                UserModel user = document.toObject(UserModel.class);

                                // Ensure fields are properly assigned
                                user.setProfession(profession);
                                user.setLocation(document.getString("location"));
                                user.setProfileImage(document.getString("profileImage"));

                                userList.add(user);
                            }
                        }

                        userAdapter.notifyDataSetChanged();
                    }
                });
    }
}
