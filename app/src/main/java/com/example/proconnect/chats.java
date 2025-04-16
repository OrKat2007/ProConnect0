package com.example.proconnect;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.proconnect.adapters.ChatsAdapter;
import com.example.proconnect.models.ChatModel;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class chats extends Fragment {

    private FirebaseFirestore db;
    private RecyclerView chatsRecyclerView;
    private ChatsAdapter chatsAdapter;
    private List<ChatModel> chatsList;
    private String currentUserEmail;
    private String dob;
    private int age;
    private String languages;
    private String availability;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        chatsList.clear();

        Query query1 = db.collection("chats")
                .whereEqualTo("user1", currentUserEmail)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        Query query2 = db.collection("chats")
                .whereEqualTo("user2", currentUserEmail)
                .orderBy("createdAt", Query.Direction.DESCENDING);

        query1.get().addOnCompleteListener(task -> handleChatQueryResult(task));
        query2.get().addOnCompleteListener(task -> handleChatQueryResult(task));
    }

    private void handleChatQueryResult(com.google.android.gms.tasks.Task<com.google.firebase.firestore.QuerySnapshot> task) {
        if (task.isSuccessful() && task.getResult() != null) {
            for (DocumentSnapshot doc : task.getResult()) {
                ChatModel chat = doc.toObject(ChatModel.class);
                if (chat != null) {
                    chat.setChatId(doc.getId());
                    chat.setLastMessage(doc.getString("LastMessage"));
                    Timestamp timestamp = doc.getTimestamp("LastMessageTimestamp");
                    long lastMessageTimestamp = (timestamp != null) ? timestamp.toDate().getTime() : 0;
                    chat.setLastMessageTimestamp(lastMessageTimestamp);

                    String partnerEmail = chat.getUser1().equals(currentUserEmail)
                            ? chat.getUser2() : chat.getUser1();

                    loadUserData(partnerEmail, (profileImage, uid, profession, location, realName) -> {
                        chat.setOtherUserName((realName != null && !realName.isEmpty()) ? realName : partnerEmail);
                        chat.setOtherUserImage((profileImage != null) ? profileImage : "");
                        chat.setOtherUserUid((uid != null) ? uid : "");
                        chat.setProfessional((profession != null) ? profession : "");
                        chat.setLocation((location != null) ? location : "");
                        chatsAdapter.notifyDataSetChanged();
                    });

                    chatsList.add(chat);
                }
            }
            chatsAdapter.updateChats(chatsList);
        }
    }

    private String formatEmail(String email) {
        return email.replace("@", "_").replace(".", "_").toLowerCase();
    }

    private void launchChatFragment(ChatModel chat) {
        if(chat.getOtherUserName().equals("Deleted User")){
            Toast.makeText(getContext(), "User is deleted", Toast.LENGTH_SHORT).show();
            return;
        }
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
        bundle.putString("dob", (dob != null ? dob : ""));
        bundle.putInt("age", age);
        bundle.putString("languages", (languages != null ? languages : ""));
        bundle.putString("availability", (availability != null ? availability : ""));

        Chat_Fragment chatFragment = new Chat_Fragment();
        chatFragment.setArguments(bundle);

        if (getActivity() != null) {
            getActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.frame_layout, chatFragment)
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void loadUserData(String email, OnUserDataLoadedListener listener) {
        db.collection("users").document(email).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String profileImage = documentSnapshot.getString("profileImage");
                        String uid = documentSnapshot.getString("uid");
                        String profession = documentSnapshot.getString("profession");
                        String location = documentSnapshot.getString("location");
                        dob = documentSnapshot.getString("dob");
                        languages = documentSnapshot.getString("languages");
                        availability = documentSnapshot.getString("availability");
                        String realName = documentSnapshot.getString("name");
                        listener.onUserDataLoaded(profileImage, uid, profession, location, realName);
                    } else {
                        listener.onUserDataLoaded("", "", "", "", "Deleted User");
                    }
                })
                .addOnFailureListener(e -> listener.onUserDataLoaded("", "", "", "", "Deleted User"));
    }

    private interface OnUserDataLoadedListener {
        void onUserDataLoaded(String profileImage, String uid, String profession, String location, String userName);
    }
}