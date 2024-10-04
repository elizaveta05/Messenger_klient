package com.example.messenger;

public class Chat {
    private String chatId;
    private String userId;
    private String userLogin;
    private String userImage;
    private String lastMessage;

    public Chat(String chatId, String userId, String userLogin, String userImage, String lastMessage) {
        this.chatId = chatId;
        this.userId = userId;
        this.userLogin = userLogin;
        this.userImage = userImage;
        this.lastMessage = lastMessage;
    }

    public Chat() {
    }

    // Getters and setters for all fields, including lastMessage
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserLogin() {
        return userLogin;
    }

    public void setUserLogin(String userLogin) {
        this.userLogin = userLogin;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }
}
