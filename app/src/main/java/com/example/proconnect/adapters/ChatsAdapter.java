package com.example.proconnect.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CircleCrop;
import com.example.proconnect.R;
import com.example.proconnect.models.ChatModel;
import com.google.firebase.firestore.FirebaseFirestore;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ChatsAdapter extends RecyclerView.Adapter<ChatsAdapter.ChatViewHolder> {
    private List<ChatModel> chats;
    private OnChatClickListener listener;

    public interface OnChatClickListener {
        void onChatClick(ChatModel chat);
    }

    public ChatsAdapter(List<ChatModel> chats) {
        this.chats = chats;
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

        // Set default chat partner name from ChatModel
        holder.tvChatInfo.setText(chat.getOtherUserName());

        // Optionally, update the UI with more partner details by querying Firestore
        // (This code can be added if you want to refresh user data)

        // Load profile image using Glide
        String imageString = chat.getOtherUserImage();
        if (imageString != null && !imageString.isEmpty()) {
            try {
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
                Glide.with(holder.itemView.getContext())
                        .load(imageString)
                        .placeholder(R.drawable.default_profile)
                        .transform(new CircleCrop())
                        .error(R.drawable.default_profile)
                        .into(holder.ivProfile);
            }
        } else {
            Glide.with(holder.itemView.getContext())
                    .load(R.drawable.default_profile)
                    .transform(new CircleCrop())
                    .into(holder.ivProfile);
        }

        // Format and display timestamp from createdAt field
        if (chat.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
            holder.tvTimestamp.setText(sdf.format(chat.getLastMessageTimestamp().toDate()));
        } else {
            holder.tvTimestamp.setText("N/A");
        }

        // Display last message if available
        if (chat.getLastMessage() != null) {
            String lastMessage = chat.getLastMessage();
            if (lastMessage.length() > 75) {
                lastMessage = lastMessage.substring(0, 75) + "...";
            }
            holder.tvLastMessage.setText(lastMessage);
        } else {
            holder.tvLastMessage.setText("No messages");
        }

        // Set click listener
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

    public static class ChatViewHolder extends RecyclerView.ViewHolder {
        LinearLayout messageContainer;
        ImageView ivProfile;
        TextView tvChatInfo, tvTimestamp, tvLastMessage;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            ivProfile = itemView.findViewById(R.id.ivProfile);
            tvChatInfo = itemView.findViewById(R.id.tvChatInfo);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
            tvLastMessage = itemView.findViewById(R.id.tvLastMessage);
        }
    }
}
