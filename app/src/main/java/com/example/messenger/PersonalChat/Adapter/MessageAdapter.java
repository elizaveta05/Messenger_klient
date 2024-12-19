package com.example.messenger.PersonalChat.Adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Message;
import com.example.messenger.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private final List<Object> messageList; // Список Object для поддержки разных типов
    private final String currentUserId;
    private final OnMessageClickListener clickListener;

    public interface OnMessageClickListener {
        void onMessageClick(Message message);
    }

    public MessageAdapter(List<Object> messageList, String currentUserId, OnMessageClickListener clickListener) {
        this.messageList = new ArrayList<>(messageList);
        this.currentUserId = currentUserId;
        this.clickListener = clickListener;
    }

    private static final int MESSAGE_DATE = 0; // Тип для даты
    private static final int MESSAGE_SENT = 1;
    private static final int MESSAGE_RECEIVED = 2;

    @Override
    public int getItemViewType(int position) {
        Object item = messageList.get(position);
        if (item instanceof String) { // Если это строка, значит дата
            return MESSAGE_DATE;
        } else if (item instanceof Message) {
            Message message = (Message) item;
            return message.getUserSend().equals(currentUserId) ? MESSAGE_SENT : MESSAGE_RECEIVED;
        }
        throw new IllegalArgumentException("Неподдерживаемый тип элемента: " + item.getClass().getName());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MESSAGE_DATE) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.date_item, parent, false);
            return new DateViewHolder(view);
        } else if (viewType == MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sending, parent, false);
            return new MessageViewHolder(view);
        } else { // MESSAGE_RECEIVED
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receiving, parent, false);
            return new MessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = messageList.get(position);

        if (item instanceof String) {
            String date = (String) item;
            ((DateViewHolder) holder).tvDate.setText(date);
        } else if (item instanceof Message) {
            Message message = (Message) item;
            MessageViewHolder messageHolder = (MessageViewHolder) holder;
            messageHolder.tvMessage.setText(message.getMessageText());

            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            String formattedTime = message.getTimeStampAsTimestamp() != null
                    ? timeFormat.format(message.getTimeStampAsTimestamp())
                    : "—"; // На случай, если парсинг даты не удался

            messageHolder.tvTime.setText(formattedTime);

            // Обработка кликов
            holder.itemView.setOnClickListener(v -> clickListener.onMessageClick(message));
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    static class DateViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate;

        public DateViewHolder(View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tv_date);
        }
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

    @SuppressLint("NotifyDataSetChanged")
    public void setMessageList(List<Object> messages) {
        this.messageList.clear();
        this.messageList.addAll(messages);
        notifyDataSetChanged();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setMessageListFromMessages(List<Message> messages) {
        this.messageList.clear();
        this.messageList.addAll(prepareMessageList(messages));
        notifyDataSetChanged();
    }

    public static List<Object> prepareMessageList(List<Message> messages) {
        List<Object> result = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());
        String lastDate = "";

        for (Message message : messages) {
            String messageDate = message.getTimeStampAsTimestamp() != null
                    ? dateFormat.format(message.getTimeStampAsTimestamp())
                    : ""; // На случай, если парсинг даты не удался

            if (!messageDate.equals(lastDate)) {
                result.add(messageDate); // Добавляем дату как строку
                lastDate = messageDate;
            }
            result.add(message); // Добавляем само сообщение
        }

        return result;
    }
}
