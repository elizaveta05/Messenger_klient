package com.example.messenger.Model;

import com.google.firebase.Timestamp;

public class RecentChats {

    private Integer chatId; // Идентификатор чата со стороны владельца
    private String userId; // Идентификатор пользователя с которым есть чат
    private String login;
    private String image_url;
    private String userSend; // Пользователь-отправитель
    private String messageText; // Текст сообщения
    private Timestamp timeStamp; // Время отправки сообщения

    // Геттеры
    public Integer getChatId() {
        return chatId;
    }

    public String getUserId() {
        return userId;
    }

    public String getLogin() {
        return login;
    }

    public String getImageUrl() {
        return image_url;
    }

    public String getUserSend() {
        return userSend;
    }

    public String getMessageText() {
        return messageText;
    }

    public Timestamp getTimeStamp() {
        return timeStamp;
    }

    // Сеттеры
    public void setChatId(Integer chatId) {
        this.chatId = chatId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public void setImageUrl(String image_url) {
        this.image_url = image_url;
    }

    public void setUserSend(String userSend) {
        this.userSend = userSend;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setTimeStamp(Timestamp timeStamp) {
        this.timeStamp = timeStamp;
    }
}
