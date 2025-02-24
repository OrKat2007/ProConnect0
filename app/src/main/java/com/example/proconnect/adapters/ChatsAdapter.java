package com.example.proconnect.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.proconnect.R;
import com.example.proconnect.models.ChatModel;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
    private List<ChatModel> chats;
    private OnChatClickListener listener;

    public ChatsAdapter(List<ChatModel> chats) {
        this.chats = chats;
    }

    // Interface for handling item clicks.
    public interface OnChatClickListener {
        void onChatClick(ChatModel chat);
    }

    public void setOnChatClickListener(OnChatClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate item_chat.xml (which must include an ImageView with id "ivProfile")
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatModel chat = chats.get(position);
        // Display the partner's real name
        holder.tvChatInfo.setText(chat.getOtherUserName());

        // Load the partner's image using Glide.
        String imageString = chat.getOtherUserImage();
        if (imageString != null && imageString.length() > 200) {
            try {
                // If the string includes a data URI scheme, split out the Base64 part.
                if (imageString.contains(",")) {
                    imageString = imageString.split(",")[1];
                }
                byte[] decodedBytes = android.util.Base64.decode(imageString, android.util.Base64.DEFAULT);
                Bitmap bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
                Glide.with(holder.itemView.getContext())
                        .load(bitmap)
                        .placeholder(R.drawable.default_profile)
                        .transform(new CircleCrop())
                        .error(R.drawable.default_profile)
                        .into(holder.ivProfile);
            } catch (Exception e) {
                e.printStackTrace();
                Glide.with(holder.itemView.getContext())
                        .load(R.drawable.default_profile)
                        .transform(new CircleCrop())
                        .into(holder.ivProfile);
            }
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(imageString)
                    .placeholder(R.drawable.default_profile)
                    .transform(new CircleCrop())
                    .error(R.drawable.default_profile)
                    .into(holder.ivProfile);
        }

        // Format and display timestamp.
        if (chat.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(chat.getCreatedAt().toDate()));
        } else {
            holder.tvTimestamp.setText("N/A");
        }
        // Display last message if available.
        if (chat.getLastMessage() != null) {
            holder.tvLastMessage.setText(chat.getLastMessage());
        } else {
            holder.tvLastMessage.setText("No messages");
        }

        // Set click listener.
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

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        TextView tvChatInfo, tvTimestamp, tvLastMessage;

        ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatInfo = itemView.findViewById(R.id.tvChatInfo);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
