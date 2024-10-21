package com.example.messenger.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Users implements Parcelable {

    private String userId;
    private String login;
    private String phoneNumber;
    private byte[] photoData;

    public Users() {
    }

    public Users(String userId, String login, String phoneNumber, byte[] photoData) {
        this.userId = userId;
        this.login = login;
        this.phoneNumber = phoneNumber;
        this.photoData = photoData;
    }

    protected Users(Parcel in) {
        userId = in.readString();
        login = in.readString();
        phoneNumber = in.readString();
        // Чтение массива байтов
        photoData = in.createByteArray();
    }

    public static final Creator<Users> CREATOR = new Creator<Users>() {
        @Override
        public Users createFromParcel(Parcel in) {
            return new Users(in);
        }

        @Override
        public Users[] newArray(int size) {
            return new Users[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(userId);
        dest.writeString(login);
        dest.writeString(phoneNumber);
        // Запись массива байтов
        dest.writeByteArray(photoData);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public byte[] getPhotoData() {
        return photoData;
    }

    public void setPhotoData(byte[] photoData) {
        this.photoData = photoData;
    }
}
