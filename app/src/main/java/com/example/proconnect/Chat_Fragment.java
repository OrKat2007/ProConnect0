package com.example.proconnect;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Chat_Fragment extends Fragment {
    private FirebaseFirestore db;
    private RecyclerView messagesRecyclerView;
    private EditText etMessage;
    private Button btnSend;
    private String chatId;
    private String currentUserEmail;
    private String chatPartnerEmail;
    private ChatAdapter chatAdapter;

    public Chat_Fragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment (make sure your layout file is named fragment_chat_)
        return inflater.inflate(R.layout.fragment_chat_, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        db = FirebaseFirestore.getInstance();

        messagesRecyclerView = view.findViewById(R.id.messagesRecyclerView);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);

        // Get current user email from FirebaseAuth
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            currentUserEmail = formatEmail(currentUser.getEmail().toLowerCase());
            Log.d("ChatFragment", "Current user email: " + currentUserEmail);
        } else {
            Toast.makeText(getContext(), "User not authenticated", Toast.LENGTH_SHORT).show();
            return;
        }

        // Retrieve chat partner email from fragment arguments
        Bundle args = getArguments();
        if (args != null) {
            chatPartnerEmail = formatEmail(args.getString("chatPartnerEmail", "").toLowerCase());
            Log.d("ChatFragment", "Chat partner email: " + chatPartnerEmail);
        } else {
            Toast.makeText(getContext(), "Chat partner info missing", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate chat ID consistently
        if (!TextUtils.isEmpty(currentUserEmail) && !TextUtils.isEmpty(chatPartnerEmail)) {
            chatId = currentUserEmail.compareTo(chatPartnerEmail) < 0
                    ? currentUserEmail + "_" + chatPartnerEmail
                    : chatPartnerEmail + "_" + currentUserEmail;
            Log.d("ChatFragment", "Generated chatId: " + chatId);
        } else {
            Toast.makeText(getContext(), "Chat ID generation failed", Toast.LENGTH_SHORT).show();
            return;
        }

        // Initialize adapter
        chatAdapter = new ChatAdapter(new ArrayList<>(), currentUserEmail);
        messagesRecyclerView.setAdapter(chatAdapter);

        // Create the chat document if it doesn't exist
        createChatIfNotExists();

        // Load chat messages
        loadChatMessages();

        btnSend.setOnClickListener(v -> {
            String messageText = etMessage.getText().toString().trim();
            if (!TextUtils.isEmpty(messageText)) {
                sendMessage(messageText);
                etMessage.setText("");
            }
        });
    }

    private String formatEmail(String email) {
        return email.replace("@", "_").replace(".", "_");
    }


    private void loadChatMessages() {
        Query messagesQuery = db.collection("chats").document(chatId)
                .collection("messages").orderBy("timestamp", Query.Direction.ASCENDING);

        messagesQuery.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e("ChatFragment", "Error loading messages", error);
                return;
            }

            if (value != null && !value.isEmpty()) {
                List<MessageModel> messagesList = new ArrayList<>();
                for (DocumentSnapshot doc : value.getDocuments()) {
                    MessageModel message = doc.toObject(MessageModel.class);
                    messagesList.add(message);
                }

                // Update adapter with the new message list
                if (chatAdapter != null) {
                    chatAdapter.updateMessages(messagesList);
                    Log.d("ChatFragment", "Messages updated. Count: " + messagesList.size());
                    messagesRecyclerView.smoothScrollToPosition(messagesList.size() - 1);
                }
            } else {
                Log.d("ChatFragment", "No messages found");
            }
        });
    }

    private void sendMessage(String text) {
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("sender", currentUserEmail);
        messageData.put("text", text);
        // Use FieldValue.serverTimestamp() so Firestore stores a Timestamp object
        messageData.put("timestamp", FieldValue.serverTimestamp());

        db.collection("chats").document(chatId)
                .collection("messages")
                .add(messageData)
                .addOnSuccessListener(documentReference -> {
                    // Message sent successfully; snapshot listener will update the UI.
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Error sending message: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }



    private void createChatIfNotExists() {
        DocumentReference chatRef = db.collection("chats").document(chatId);

        chatRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (!document.exists()) {
                    Map<String, Object> chatData = new HashMap<>();
                    chatData.put("user1", currentUserEmail);
                    chatData.put("user2", chatPartnerEmail);
                    chatData.put("createdAt", FieldValue.serverTimestamp());

                    chatRef.set(chatData).addOnSuccessListener(aVoid -> {
                        Log.d("ChatFragment", "Chat created successfully");
                        // Optionally delay loading messages to ensure chat creation
                        new Handler(Looper.getMainLooper()).postDelayed(this::loadChatMessages, 100);
                    }).addOnFailureListener(e ->
                            Log.e("ChatFragment", "Failed to create chat", e)
                    );
                } else {
                    Log.d("ChatFragment", "Chat already exists");
                    new Handler(Looper.getMainLooper()).postDelayed(this::loadChatMessages, 100);
                }
            } else {
                Log.e("ChatFragment", "Error checking chat existence", task.getException());
            }
        });
    }
}
