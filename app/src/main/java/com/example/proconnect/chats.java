package com.example.proconnect;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class chats extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView chatsRecyclerView;
    private ChatsAdapter chatsAdapter;
    private List<ChatModel> chatsList;
    private String currentUserEmail;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Initialize RecyclerView after the layout is fully created
        chatsRecyclerView = view.findViewById(R.id.recyclerViewchats);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsList = new ArrayList<>();
        chatsAdapter = new ChatsAdapter(chatsList);
        chatsRecyclerView.setAdapter(chatsAdapter);

        loadChats();
    }

    private void loadChats() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = formatEmail(currentUser.getEmail().toLowerCase());
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("chats")
                .whereArrayContains("participants", currentUserEmail)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("chats", "Error loading chats", error);
                        return;
                    }
                    if (snapshot != null && !snapshot.isEmpty()) {
                        List<ChatModel> chatModels = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            ChatModel chatModel = doc.toObject(ChatModel.class);
                            if (chatModel != null) {
                                chatModel.setChatId(doc.getId());
                                chatModels.add(chatModel);
                            }
                        }
                        chatsAdapter.updateChats(chatModels);
                    } else {
                        Log.d("chats", "No chats found");
                    }
                });
    }

    private String formatEmail(String email) {
        return email.replace("@", "_").replace(".", "_").toLowerCase();
    }
}