package com.example.messenger.Model;
import com.google.gson.annotations.SerializedName;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class Message {
    @SerializedName("message_id")
    private Integer message_id;

    @SerializedName("chat_id")
    private Integer chat_id;

    @SerializedName("sender_user")
    private String sender_user;

    @SerializedName("message_text")
    private String message_text;

    @SerializedName("time_stamp")
    private String time_stamp;

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

    public String getTime_stamp() {
        return time_stamp;
    }

    public void setTimeCreated(String time_stamp) {
        this.time_stamp = time_stamp;
    }
    public Timestamp getTimeStampAsTimestamp() {
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            return new Timestamp(format.parse(time_stamp).getTime());
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }
}