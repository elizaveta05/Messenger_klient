package com.example.messenger.PersonalChat.Adapter;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Messages;
import com.example.messenger.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // Поля адаптера
    private final List<Messages> messageList; // Список сообщений
    private final String currentUserId; // ID текущего пользователя для определения, кто отправил сообщение
    private final OnMessageClickListener clickListener; // Слушатель кликов на сообщения

    // Интерфейс для обработки кликов на сообщения
    public interface OnMessageClickListener {
        void onMessageClick(Messages message);
    }

    // Конструктор адаптера
    public MessageAdapter(List<Messages> messageList, String currentUserId, OnMessageClickListener clickListener) {
        this.messageList = new ArrayList<>(messageList); // Копируем список сообщений
        this.currentUserId = currentUserId; // Сохраняем ID текущего пользователя
        this.clickListener = clickListener; // Устанавливаем слушатель кликов
    }

    // Константы типов элементов
    private static final int MESSAGE_SENT = 1; // Тип элемента для отправленного сообщения
    private static final int MESSAGE_RECEIVED = 2; // Тип элемента для полученного сообщения

    // Определение типа элемента в списке
    @Override
    public int getItemViewType(int position) {
        Messages message = messageList.get(position);
        // Проверяем, является ли сообщение отправленным текущим пользователем
        return message.getUserSend().equals(currentUserId) ? MESSAGE_SENT : MESSAGE_RECEIVED;
    }

    // Создание ViewHolder в зависимости от типа элемента
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (viewType == MESSAGE_SENT) { // Для отправленного сообщения
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sending, parent, false);
            return new MessageViewHolder(view);
        } else { // Для полученного сообщения
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_receiving, parent, false);
            return new MessageViewHolder(view);
        }
    }

    // Привязка данных к ViewHolder
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Messages message = messageList.get(position);
        MessageViewHolder messageHolder = (MessageViewHolder) holder;

        // Устанавливаем текст сообщения
        messageHolder.tvMessage.setText(message.getMessageText());

        // Форматируем и устанавливаем время
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        @SuppressLint("SimpleDateFormat")
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault());

        String formattedTime = "—"; // Значение по умолчанию
        if (message.getTimeStampAsTimestamp() != null) {
            Date messageDate = message.getTimeStampAsTimestamp();
            Date today = new Date();

            // Сравниваем дату сообщения с сегодняшним днём
            SimpleDateFormat dayFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
            if (dayFormat.format(messageDate).equals(dayFormat.format(today))) {
                // Если сообщение из сегодняшнего дня, показываем только время
                formattedTime = timeFormat.format(messageDate);
            } else {
                // Если сообщение не из сегодняшнего дня, показываем дату и время
                formattedTime = dateFormat.format(messageDate) + " " + timeFormat.format(messageDate);
            }
        }

        // Устанавливаем текст времени/даты
        messageHolder.tvTime.setText(formattedTime);

        // Устанавливаем обработчик клика
        holder.itemView.setOnClickListener(v -> clickListener.onMessageClick(message));
    }

    // Количество элементов в списке
    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder для сообщений
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage; // Текст сообщения
        TextView tvTime; // Время сообщения

        public MessageViewHolder(View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.tv_message); // Находим TextView для текста сообщения
            tvTime = itemView.findViewById(R.id.tv_time); // Находим TextView для времени сообщения
        }
    }

    // Обновление списка сообщений
    @SuppressLint("NotifyDataSetChanged")
    public void setMessageList(List<Messages> messages) {
        this.messageList.clear(); // Очистка текущего списка
        this.messageList.addAll(messages); // Добавление новых сообщений
        notifyDataSetChanged(); // Обновление интерфейса
    }
}
