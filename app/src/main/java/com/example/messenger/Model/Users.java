package com.example.messenger.Model;

import android.os.Parcel;
import android.os.Parcelable;

public class Users implements Parcelable {

    private String userId;
    private String login;
    private String phoneNumber;
    private String image_url;

    public Users() {
    }

    public Users(String userId, String login, String phoneNumber, String image_url) {
        this.userId = userId;
        this.login = login;
        this.phoneNumber = phoneNumber;
        this.image_url = image_url;
    }

    protected Users(Parcel in) {
        userId = in.readString();
        login = in.readString();
        phoneNumber = in.readString();

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
        dest.writeString(image_url);
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

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }
}
