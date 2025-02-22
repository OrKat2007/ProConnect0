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
        // Inflate fragment_chats.xml
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatsRecyclerView = view.findViewById(R.id.recyclerViewchats);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatsList = new ArrayList<>();
        chatsAdapter = new ChatsAdapter(chatsList);
        chatsRecyclerView.setAdapter(chatsAdapter);

        // Set click listener to launch Chat_Fragment when an item is tapped.
        chatsAdapter.setOnChatClickListener(chat -> launchChatFragment(chat));

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

        // Query for chats where current user is user1.
        db.collection("chats")
                .whereEqualTo("user1", currentUserEmail)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            ChatModel chat = doc.toObject(ChatModel.class);
                            if (chat != null) {
                                chat.setChatId(doc.getId());
                                // Determine partner's email.
                                // Inside your loadChats() queries when processing each chat:
                                String partnerEmail = chat.getUser1().equals(currentUserEmail)
                                        ? chat.getUser2() : chat.getUser1();
                                loadUserData(partnerEmail, new OnUserDataLoadedListener() {
                                    @Override
                                    public void onUserDataLoaded(String profileImage, String uid, String profession, String location, String realName) {
                                        chat.setOtherUserName(realName != null ? realName : partnerEmail);
                                        chat.setOtherUserImage(profileImage != null ? profileImage : "");
                                        chat.setOtherUserUid(uid != null ? uid : "");
                                        chat.setProfessional(profession != null ? profession : "");
                                        chat.setLocation(location != null ? location : "");
                                        chatsAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        chat.setOtherUserName(partnerEmail);
                                        chat.setOtherUserImage("");
                                        chat.setOtherUserUid("");
                                        chat.setProfessional("");
                                        chat.setLocation("");
                                        chatsAdapter.notifyDataSetChanged();
                                    }
                                });

                                chatsList.add(chat);
                            }
                        }
                        chatsAdapter.updateChats(chatsList);
                    }
                });

        // Query for chats where current user is user2.
        db.collection("chats")
                .whereEqualTo("user2", currentUserEmail)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        for (DocumentSnapshot doc : task.getResult()) {
                            ChatModel chat = doc.toObject(ChatModel.class);
                            if (chat != null) {
                                chat.setChatId(doc.getId());
                                // Inside your loadChats() queries when processing each chat:
                                String partnerEmail = chat.getUser1().equals(currentUserEmail)
                                        ? chat.getUser2() : chat.getUser1();
                                loadUserData(partnerEmail, new OnUserDataLoadedListener() {
                                    @Override
                                    public void onUserDataLoaded(String profileImage, String uid, String profession, String location, String realName) {
                                        chat.setOtherUserName(realName != null ? realName : partnerEmail);
                                        chat.setOtherUserImage(profileImage != null ? profileImage : "");
                                        chat.setOtherUserUid(uid != null ? uid : "");
                                        chat.setProfessional(profession != null ? profession : "");
                                        chat.setLocation(location != null ? location : "");
                                        chatsAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onError(Exception e) {
                                        chat.setOtherUserName(partnerEmail);
                                        chat.setOtherUserImage("");
                                        chat.setOtherUserUid("");
                                        chat.setProfessional("");
                                        chat.setLocation("");
                                        chatsAdapter.notifyDataSetChanged();
                                    }
                                });

                                chatsList.add(chat);
                            }
                        }
                        chatsAdapter.updateChats(chatsList);
                    }
                });
    }

    private String formatEmail(String email) {
        return email.replace("@", "_").replace(".", "_").toLowerCase();
    }

    // Launch Chat_Fragment and pass along the chat data and partner details.
    private void launchChatFragment(ChatModel chat) {
        Chat_Fragment chatFragment = new Chat_Fragment();
        Bundle bundle = new Bundle();
        bundle.putString("chatId", chat.getChatId());
        String partnerEmail = chat.getUser1().equals(currentUserEmail)
                ? chat.getUser2() : chat.getUser1();
        bundle.putString("chatPartnerEmail", partnerEmail);
        bundle.putString("chatPartnerImage", chat.getOtherUserImage());
        bundle.putString("userName", chat.getOtherUserName());
        bundle.putString("chatPartnerUid", chat.getOtherUserUid());
        bundle.putString("profession", chat.getProfessional());
        bundle.putString("location", chat.getLocation());
        chatFragment.setArguments(bundle);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, chatFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }


    // Callback interface for loading partner details.
    private interface OnUserDataLoadedListener {
        void onUserDataLoaded(String profileImage, String uid, String profession, String location, String userName);
        void onError(Exception e);
    }

    // Load partner details from the "users" collection using the partner's formatted email.
    private void loadUserData(String email, OnUserDataLoadedListener listener) {
        db.collection("users").document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImage = documentSnapshot.getString("profileImage");
                        String uid = documentSnapshot.getString("uid");
                        String profession = documentSnapshot.getString("profession");
                        String location = documentSnapshot.getString("location");
                        // Retrieve the real name from the "name" field.
                        String realName = documentSnapshot.getString("name");
                        listener.onUserDataLoaded(profileImage, uid, profession, location, realName);
                    } else {
                        listener.onError(new Exception("User not found"));
                    }
                })
                .addOnFailureListener(listener::onError);
    }

}
