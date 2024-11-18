package com.example.messenger.Chat.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Message;
import com.example.messenger.R;

import java.text.SimpleDateFormat;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private String currentUserId;

    public MessageAdapter(List<Message> messageList, String currentUserId) {
        this.messageList = messageList;
        this.currentUserId = currentUserId;
    }

    private static final int MESSAGE_SENT = 1;
    private static final int MESSAGE_RECEIVED = 2;

    @Override
    public int getItemViewType(int position) {
        Message message = messageList.get(position);
        if (message.getSenderId().equals(currentUserId)) {
            return MESSAGE_SENT;
        } else {
            return MESSAGE_RECEIVED;
        }
    }
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sending, parent, false);
        } else {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receiving, parent, false);
        }
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);

        holder.tvMessage.setText(message.getContent());

        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
        holder.tvTime.setText(dateFormat.format(message.getTimestamp()));
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {

        TextView tvMessage;
        TextView tvTime;

        public MessageViewHolder(View itemView) {
            super(itemView);

            tvMessage = itemView.findViewById(R.id.tv_message);
            tvTime = itemView.findViewById(R.id.tv_time);

        }
    }
}
