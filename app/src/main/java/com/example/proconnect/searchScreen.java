package com.example.proconnect;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

public class searchScreen extends Fragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<usermodel> userList;
    private FirebaseFirestore firestore;

    public searchScreen() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search_screen, container, false);

        etSearch = view.findViewById(R.id.etSearch);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();
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
                .whereEqualTo("professional", true) // Filter only professionals
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        userList = new ArrayList<>();
                        userAdapter = new UserAdapter(userList, getContext());  // Pass context, not the query
                        recyclerView.setAdapter(userAdapter);

                        userList.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            usermodel user = document.toObject(usermodel.class);
                            if (user.getProfession() != null && user.getProfession().toLowerCase().contains(query.toLowerCase())) {

                                userList.add(user);
                            }
                        }
                        userAdapter.notifyDataSetChanged();
                    }
                });
    }
}
