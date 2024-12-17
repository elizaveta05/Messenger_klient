package com.example.messenger.Model;

import com.google.firebase.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecentChats {

    private Integer chatId;
    private String userId;
    private String login;
    private String image_url;
    private String userSend;
    private String messageText;
    private String timeStamp;
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

    public String getTimeStamp() {
        return timeStamp;
    }

    // Дополнительный метод для получения даты
    public Date getTimeStampAsDate() {
        if (timeStamp != null) {
            try {
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX").parse(timeStamp);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
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

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
