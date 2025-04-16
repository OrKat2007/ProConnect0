package com.example.proconnect.adapters;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.proconnect.R;
import com.example.proconnect.models.MessageModel;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<MessageModel> messageList;
    private String currentUserName; // Now using user name

    public MessageAdapter(List<MessageModel> messageList, String currentUserName) {
        this.messageList = messageList;
        this.currentUserName = currentUserName;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        MessageModel message = messageList.get(position);
        holder.tvMessage.setText(message.getText());

        // Format timestamp to a human-readable string
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
        if (message.getTimestamp() != null) {
            holder.tvTimestamp.setText(sdf.format(message.getTimestamp().toDate()));
        } else {
            holder.tvTimestamp.setText("N/A");
        }

        // Check if the message was sent by the current user by comparing sender names
        if (message.getSender().equals(currentUserName)) {
            // For sent messages, align the message container to the right
            holder.messageContainer.setGravity(Gravity.END);
            holder.tvSender.setText("You");
        } else {
            // For received messages, align the container to the left
            holder.messageContainer.setGravity(Gravity.START);
            holder.tvSender.setText(message.getSender());
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // Method to update messages in the adapter
    public void updateMessages(List<MessageModel> newMessages) {
        this.messageList = newMessages;
        notifyDataSetChanged();
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvSender, tvMessage, tvTimestamp;
        LinearLayout messageContainer;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            // Make sure your item_message.xml contains a LinearLayout with id messageContainer
            messageContainer = itemView.findViewById(R.id.messageContainer);
            tvSender = itemView.findViewById(R.id.tvSender);
            tvMessage = itemView.findViewById(R.id.tvMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
