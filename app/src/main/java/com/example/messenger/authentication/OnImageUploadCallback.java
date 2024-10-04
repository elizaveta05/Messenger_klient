package com.example.messenger.authentication;

public interface OnImageUploadCallback {
    void onUploadSuccess(String imageUrl);
    void onUploadFailure(String errorMessage);
}
