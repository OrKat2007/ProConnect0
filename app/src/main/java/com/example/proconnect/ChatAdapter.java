package com.example.proconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatModel> chats;
    private OnChatClickListener listener;

    public ChatAdapter(List<ChatModel> chats) {
        this.chats = chats;
    }

    // Interface for handling item clicks
    public interface OnChatClickListener {
        void onChatClick(ChatModel chat);
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chats.get(position);

        // Get the current user's formatted email
        String currentUserFormattedEmail = formatEmail(
                FirebaseAuth.getInstance().getCurrentUser().getEmail().toLowerCase()
        );

        // Determine the "other" user in the chat
        String otherUser;
        if (chat.getUser1().equals(currentUserFormattedEmail)) {
            otherUser = chat.getUser2();
        } else {
            otherUser = chat.getUser1();
        }

        // Display only the other user's email (or name if you have that data)
        holder.tvChatInfo.setText(otherUser);

        // Format and display the creation timestamp
        if (chat.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(chat.getCreatedAt().toDate()));
        } else {
            holder.tvTimestamp.setText("N/A");
        }

        // Optionally display the last message preview
        if (chat.getLastMessage() != null) {
            holder.tvLastMessage.setText(chat.getLastMessage());
        } else {
            holder.tvLastMessage.setText("No messages");
        }

        // Set a click listener on the entire item view.
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onChatClick(chat);
            }
        });
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void updateChats(List<ChatModel> newChats) {
        this.chats = newChats;
        notifyDataSetChanged();
    }

    private String formatEmail(String email) {
        return email.replace("@", "_").replace(".", "_");
    }

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatInfo, tvTimestamp, tvLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatInfo = itemView.findViewById(R.id.tvChatInfo);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
