package com.example.messenger.Authentication;

public interface OnImageUploadCallback {
    void onUploadSuccess(String imageUrl);
    void onUploadFailure(String errorMessage);
}
