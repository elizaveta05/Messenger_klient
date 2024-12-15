package com.example.messenger.Model;

import java.sql.Timestamp;
import java.time.Instant;

public class Message {
    private Integer message_id;
    private Integer chat_id;
    private String sender_user;
    private String message_text;
    private Timestamp time_stamp;

    // Геттеры и сеттеры
    public Integer getMessageId() {
        return message_id;
    }

    public void setMessageId(Integer message_id) {
        this.message_id = message_id;
    }

    public Integer getChatId() {
        return chat_id;
    }

    public void setChatId(Integer chat_id) {
        this.chat_id = chat_id;
    }

    public String getUserSend() {
        return sender_user;
    }

    public void setUserSend(String sender_user) {
        this.sender_user = sender_user;
    }

    public String getMessageText() {
        return message_text;
    }

    public void setMessageText(String message_text) {
        this.message_text = message_text;
    }

    public Timestamp getTime_stamp() {
        return time_stamp;
    }

    public void setTimeCreated(Timestamp time_stamp) {
        this.time_stamp = time_stamp;
    }

}