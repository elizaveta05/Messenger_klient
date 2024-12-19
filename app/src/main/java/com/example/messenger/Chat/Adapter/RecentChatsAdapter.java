package com.example.messenger.Chat.Adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.RecentChats;
import com.example.messenger.R;
import com.google.firebase.Timestamp;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class RecentChatsAdapter extends RecyclerView.Adapter<RecentChatsAdapter.ViewHolder> {

    private List<RecentChats> chatList;
    private final LayoutInflater inflater;
    private final OnUserClickListener onUserClickListener;

    private final OnChatLongClickListener onChatLongClickListener;

    public RecentChatsAdapter(Context context, List<RecentChats> chatList, OnUserClickListener listener, OnChatLongClickListener longClickListener) {
        this.inflater = LayoutInflater.from(context);
        this.chatList = chatList;
        this.onUserClickListener = listener;
        this.onChatLongClickListener = longClickListener;
    }


    // Метод для сортировки списка по временным меткам
    public void sortChatsByTime() {
        if (chatList != null && !chatList.isEmpty()) {
            Collections.sort(chatList, (chat1, chat2) -> {
                try {
                    // Парсим временные метки в объекты Date
                    SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                    java.util.Date date1 = inputDateFormat.parse(chat1.getTimeStamp());
                    java.util.Date date2 = inputDateFormat.parse(chat2.getTimeStamp());

                    // Сравниваем даты
                    return date2.compareTo(date1);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return 0;
            });
        }
    }

    // Метод для обновления данных в списке и сортировки
    @SuppressLint("NotifyDataSetChanged")
    public void updateChatList(List<RecentChats> newChatList) {
        this.chatList = newChatList;
        sortChatsByTime(); // Сортируем новый список
        notifyDataSetChanged(); // Уведомляем об изменении данных
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.recent_chat_adapter, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        RecentChats chat = chatList.get(position);

        // Устанавливаем имя пользователя
        holder.userName.setText(chat.getLogin());

        // Проверка, отправлено ли сообщение текущим пользователем
        String messageText = chat.getMessageText();
        if (!(chat.getUserId().equals(chat.getUserSend()))) {
            messageText = "Ты: " + messageText;
        }

        // Обрезаем текст сообщения до 15 символов с троеточием
        if (messageText != null && messageText.length() > 15) {
            messageText = messageText.substring(0, 15) + "...";
        }
        holder.userMessage.setText(messageText);

        // Форматируем время отправки сообщения
        if (chat.getTimeStamp() != null) {
            String formattedTime = formatMessageTime(chat.getTimeStamp());
            holder.userLastMessageTime.setText(formattedTime);
        }

        // Загрузка изображения
        if (chat.getImageUrl() != null && !chat.getImageUrl().isEmpty()) {
            Picasso.get()
                    .load(chat.getImageUrl())
                    .placeholder(R.drawable.icon_user)
                    .error(R.drawable.icon_user)
                    .into(holder.userImage);
        } else {
            loadImageFromFirebaseStorage(chat.getUserId(), holder.userImage);
        }

        // Устанавливаем клик-событие
        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(chat);
            }
        });

        holder.itemView.setOnLongClickListener(v -> {
            if (onChatLongClickListener != null) {
                onChatLongClickListener.onChatLongClick(v, chat);
            }
            return true;
        });

    }

    /**
     * Форматирует время сообщения в зависимости от текущей даты.
     */
    @SuppressLint("SimpleDateFormat")
    private String formatMessageTime(String timeStamp) {
        SimpleDateFormat todayFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM", new Locale("ru")); // Указываем локализацию на русский
        SimpleDateFormat inputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", new Locale("ru"));

        java.util.Date messageDate = null;
        try {
            // Парсим дату из строки
            messageDate = inputDateFormat.parse(timeStamp);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        if (messageDate == null) {
            return ""; // Если дату не удалось распарсить, возвращаем пустую строку
        }

        long now = System.currentTimeMillis();
        long messageTimeMillis = messageDate.getTime();

        // Сравниваем даты
        SimpleDateFormat checkDateFormat = new SimpleDateFormat("yyyyMMdd",new Locale("ru"));
        String currentDate = checkDateFormat.format(now);
        String messageDateStr = checkDateFormat.format(messageTimeMillis);

        if (currentDate.equals(messageDateStr)) {
            // Если сообщение сегодняшнее, показываем только время
            return todayFormat.format(messageTimeMillis);
        } else {
            // Если сообщение из другого дня, показываем день и месяц
            return dateFormat.format(messageTimeMillis);
        }
    }


    @Override
    public int getItemCount() {
        return chatList.size();
    }

    private void loadImageFromFirebaseStorage(String userId, CircleImageView imageView) {
        if (userId == null || userId.isEmpty()) return;

        StorageReference storageReference = FirebaseStorage.getInstance()
                .getReference("users_profile_image/" + userId + ".jpg");

        storageReference.getDownloadUrl().addOnSuccessListener(uri ->
                Picasso.get()
                        .load(uri)
                        .placeholder(R.drawable.icon_user)
                        .error(R.drawable.icon_user)
                        .into(imageView)
        ).addOnFailureListener(e -> {
            Log.e("RecentChatsAdapter", "Failed to load image for userId: " + userId, e);
            imageView.setImageResource(R.drawable.icon_user);
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userMessage, userLastMessageTime;
        CircleImageView userImage;

        public ViewHolder(View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.tv_name_user);
            userImage = itemView.findViewById(R.id.image_photo_user);
            userMessage = itemView.findViewById(R.id.tv_message);
            userLastMessageTime = itemView.findViewById(R.id.tv_time);
        }
    }

    public interface OnUserClickListener {
        void onUserClick(RecentChats chat);
    }

    public interface OnChatLongClickListener {
        void onChatLongClick(View view, RecentChats chat);
    }

}
