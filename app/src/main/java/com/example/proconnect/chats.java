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
    private ChatAdapter chatsAdapter;
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
        // Initialize RecyclerView after layout is ready
        chatsRecyclerView = view.findViewById(R.id.recyclerViewchats);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsList = new ArrayList<>();
        chatsAdapter = new ChatAdapter(chatsList);
        chatsRecyclerView.setAdapter(chatsAdapter);

        // Set the click listener for chat items.
        chatsAdapter.setOnChatClickListener(chat -> {
            // Determine chat partner's formatted email.
            String chatPartnerEmail;
            if (chat.getUser1().equals(currentUserEmail)) {
                chatPartnerEmail = chat.getUser2();
            } else {
                chatPartnerEmail = chat.getUser1();
            }
            // Load the chat partner's details from the "users" collection.
            loadUserData(chatPartnerEmail, new OnUserDataLoadedListener() {
                @Override
                public void onUserDataLoaded(String profileImage, String uid, String profession, String location, String userName) {
                    launchChatFragment(chat, chatPartnerEmail, profileImage, uid, profession, location, userName);
                }

                @Override
                public void onError(Exception e) {
                    // If loading fails, launch Chat_Fragment with default values.
                    launchChatFragment(chat, chatPartnerEmail, "", "", "", "", "");
                }
            });
        });

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

        // Clear previous data.
        chatsList.clear();

        // Query for chats where the current user is user1.
        db.collection("chats")
                .whereEqualTo("user1", currentUserEmail)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("chats", "Error loading chats", error);
                        return;
                    }
                    if (snapshot != null) {
                        List<ChatModel> chatModels = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            ChatModel chatModel = doc.toObject(ChatModel.class);
                            if (chatModel != null) {
                                chatModel.setChatId(doc.getId());
                                chatModels.add(chatModel);
                            }
                        }
                        mergeChats(chatModels);
                    }
                });

        // Query for chats where the current user is user2.
        db.collection("chats")
                .whereEqualTo("user2", currentUserEmail)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        Log.e("chats", "Error loading chats", error);
                        return;
                    }
                    if (snapshot != null) {
                        List<ChatModel> chatModels = new ArrayList<>();
                        for (DocumentSnapshot doc : snapshot.getDocuments()) {
                            ChatModel chatModel = doc.toObject(ChatModel.class);
                            if (chatModel != null) {
                                chatModel.setChatId(doc.getId());
                                chatModels.add(chatModel);
                            }
                        }
                        mergeChats(chatModels);
                    }
                });
    }

    // Merge new chat results and update the adapter.
    private void mergeChats(List<ChatModel> newChats) {
        // For each chat, compute the "other" user's display value.
        for (ChatModel chat : newChats) {
            String otherUser;
            if (chat.getUser1().equals(currentUserEmail)) {
                otherUser = chat.getUser2();
            } else {
                otherUser = chat.getUser1();
            }
            chat.setOtherUserName(otherUser);
        }
        chatsList.addAll(newChats);
        // Optionally remove duplicates or sort the list.
        chatsAdapter.updateChats(chatsList);
    }

    private String formatEmail(String email) {
        return email.replace("@", "_").replace(".", "_").toLowerCase();
    }

    // Launch Chat_Fragment with the loaded chat and partner details.
    private void launchChatFragment(ChatModel chat, String chatPartnerEmail,
                                    String profileImage, String uid,
                                    String profession, String location, String userName) {
        Chat_Fragment chatFragment = new Chat_Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("chatId", chat.getChatId());
        bundle.putString("chatPartnerEmail", chatPartnerEmail);
        bundle.putString("chatPartnerImage", profileImage);
        bundle.putString("chatPartnerUid", uid);
        bundle.putString("profession", profession);
        bundle.putString("location", location);
        bundle.putString("userName", userName);
        chatFragment.setArguments(bundle);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, chatFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    // Interface for receiving loaded user data.
    private interface OnUserDataLoadedListener {
        void onUserDataLoaded(String profileImage, String uid, String profession, String location, String userName);
        void onError(Exception e);
    }

    // Load user details from the "users" collection for the given formatted email.
    private void loadUserData(String email, OnUserDataLoadedListener listener) {
        db.collection("users").document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImage = documentSnapshot.getString("profileImage");
                        String uid = documentSnapshot.getString("uid");
                        String profession = documentSnapshot.getString("profession");
                        String location = documentSnapshot.getString("location");
                        String userName = documentSnapshot.getString("name");
                        listener.onUserDataLoaded(profileImage, uid, profession, location, userName);
                    } else {
                        listener.onError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(listener::onError);
    }
}
