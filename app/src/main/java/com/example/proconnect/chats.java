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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.DocumentSnapshot;

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

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chats, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        chatsRecyclerView = view.findViewById(R.id.recyclerViewchats);
        chatsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        chatsList    = new ArrayList<>();
        chatsAdapter = new ChatsAdapter(chatsList);
        chatsRecyclerView.setAdapter(chatsAdapter);
        chatsAdapter.setOnChatClickListener(this::launchChatFragment);

        loadChats();
    }

    private void loadChats() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            Toast.makeText(getContext(),
                    "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }
        currentUserEmail = user.getEmail().toLowerCase()
                .replace("@","_").replace(".","_");

        db.collection("chats")
                .whereArrayContains("participants", currentUserEmail)
                .orderBy("LastMessageTimestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snap, err) -> {
                    if (err != null || snap == null) return;

                    chatsList.clear();
                    for (DocumentSnapshot doc : snap.getDocuments()) {
                        ChatModel chat = doc.toObject(ChatModel.class);
                        if (chat == null) continue;
                        chat.setChatId(doc.getId());

                        Timestamp ts = doc.getTimestamp("LastMessageTimestamp");
                        chat.setLastMessageTimestamp(
                                ts != null ? ts.toDate().getTime() : 0L
                        );

                        // Determine partner key from `participants` list
                        List<String> parts = chat.getParticipants();
                        String partner = parts.get(0).equals(currentUserEmail)
                                ? parts.get(1)
                                : parts.get(0);

                        // Load partner profile; if missing, show "(deleted)"
                        db.collection("users").document(partner)
                                .get()
                                .addOnSuccessListener(ds -> {
                                    if (ds.exists()) {
                                        chat.setOtherUserUid(partner);
                                        chat.setOtherUserName(ds.getString("name"));
                                        chat.setOtherUserImage(ds.getString("profileImage"));
                                        chat.setProfessional(ds.getString("profession"));
                                        chat.setLocation(ds.getString("location"));
                                        chat.setDob(ds.getString("dob"));
                                        chat.setAvailability(ds.getString("availability"));
                                        chat.setLanguages(ds.getString("languages"));
                                    } else {
                                        // user doc was deleted → display fallback
                                        chat.setOtherUserUid(partner);
                                        chat.setOtherUserName(partner + " (deleted)");
                                        chat.setOtherUserImage(null);
                                        chat.setProfessional("");
                                        chat.setLocation("");
                                        chat.setDob("");
                                        chat.setAvailability("");
                                        chat.setLanguages("");
                                    }
                                    chatsAdapter.notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> {
                                    // network / permission error → also show deleted fallback
                                    chat.setOtherUserUid(partner);
                                    chat.setOtherUserName(partner + " (deleted)");
                                    chatsAdapter.notifyDataSetChanged();
                                });

                        chatsList.add(chat);
                    }
                    chatsAdapter.updateChats(chatsList);
                });
    }

    private void launchChatFragment(ChatModel chat) {
        Bundle b = new Bundle();
        b.putString("chatId", chat.getChatId());
        b.putString("chatPartnerUid", chat.getOtherUserUid());
        b.putString("chatPartnerImage", chat.getOtherUserImage());
        b.putString("userName", chat.getOtherUserName());
        b.putString("profession", chat.getProfessional());
        b.putString("location", chat.getLocation());
        b.putString("dob", chat.getDob());
        b.putString("languages", chat.getLanguages());
        b.putString("availability", chat.getAvailability());
        // mark fragment state if partner was deleted
        b.putBoolean("isUserDeleted",
                chat.getOtherUserName().endsWith("(deleted)")
        );

        Chat_Fragment frag = new Chat_Fragment();
        frag.setArguments(b);
        if (getActivity() != null) {
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frame_layout, frag)
                    .addToBackStack(null)
                    .commit();
        }
    }
}
