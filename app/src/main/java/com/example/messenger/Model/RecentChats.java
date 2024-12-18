package com.example.messenger.Model;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class RecentChats implements Parcelable {

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

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
    public RecentChats(String userSend) {
        this.userSend = userSend;
    }

    // Геттер
    public String getUserSend() {
        return userSend;
    }

    // Сеттер
    public void setUserSend(String userSend) {
        this.userSend = userSend;
    }

    // Parcelable реализация
    protected RecentChats(Parcel in) {
        userSend = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userSend);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Parcelable.Creator<RecentChats> CREATOR = new Creator<RecentChats>() {
        @Override
        public RecentChats createFromParcel(Parcel in) {
            return new RecentChats(in);
        }

        @Override
        public RecentChats[] newArray(int size) {
            return new RecentChats[size];
        }
    };
}
