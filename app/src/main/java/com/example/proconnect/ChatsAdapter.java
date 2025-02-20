package com.example.proconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
    private List<ChatModel> chats;

    public ChatsAdapter(List<ChatModel> chats) {
        this.chats = chats;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate your chat item layout (e.g., item_chat.xml)
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chats.get(position);
        // Display a combination of user emails or the other user's name.
        holder.tvChatInfo.setText(chat.getUser1() + " & " + chat.getUser2());

        // Format and display the creation timestamp
        if (chat.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(chat.getCreatedAt().toDate()));
        } else {
            holder.tvTimestamp.setText("N/A");
        }

        // Optionally display the last message if available
        if (chat.getLastMessage() != null) {
            holder.tvLastMessage.setText(chat.getLastMessage());
        }
    }

    @Override
    public int getItemCount() {
        return chats.size();
    }

    public void updateChats(List<ChatModel> newChats) {
        this.chats = newChats;
        notifyDataSetChanged();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView tvChatInfo, tvTimestamp, tvLastMessage;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            tvChatInfo = itemView.findViewById(R.id.tvChatInfo);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
