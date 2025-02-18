package com.example.proconnect;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {
    private List<MessageModel> messageList;
    private String currentUserEmail;

    public ChatAdapter(List<MessageModel> messageList, String currentUserEmail) {
        this.messageList = messageList;
        this.currentUserEmail = currentUserEmail;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.tvMessage.setText(message.getText());

        // Format timestamp to a human-readable string using the Timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        if (message.getTimestamp() != null) {
            holder.tvTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
        } else {
            holder.tvTimestamp.setText("N/A");
        }

        // Display "You" if the message is sent by current user; otherwise, show sender email
        if (message.getSender().equals(currentUserEmail)) {
            holder.tvSender.setText("You");
        } else {
            holder.tvSender.setText(message.getSender());
        }
    }


    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // **New updateMessages method**
    public void updateMessages(List<MessageModel> newMessages) {
        this.messageList = newMessages;
        notifyDataSetChanged(); // Refresh the RecyclerView
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessage, tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}

