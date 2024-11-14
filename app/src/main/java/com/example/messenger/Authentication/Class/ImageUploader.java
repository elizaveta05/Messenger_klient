package com.example.messenger.Authentication.Class;

import android.graphics.Bitmap;
import android.util.Log;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

public class ImageUploader {
    private StorageReference storageRef;

    public ImageUploader() {
        // Инициализация Firebase Storage
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();
    }

    // Метод для загрузки изображения в Firebase Storage
    public void uploadImage(FirebaseUser user, Bitmap selectedImageBitmap, ImageUploadCallback callback) {
        if (selectedImageBitmap != null) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            String imagePath = "users_profile_image/" + user.getUid() + ".jpg"; // Путь к изображению
            StorageReference imageRef = storageRef.child(imagePath);
            // Сохранение изображения в хранилище
            UploadTask uploadTask = imageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    Log.d("FirebaseStorage", "Изображение успешно добавлено в Firebase Storage");
                    callback.onSuccess(imageUrl); // Вызов успеха с URL
                });
            }).addOnFailureListener(e -> {
                Log.e("FirebaseStorage", "Ошибка загрузки изображения: " + e.getMessage());
                callback.onError(e.getMessage()); // Вызов ошибки
            });
        } else {
            callback.onError("Изображение не выбрано.");
        }
    }

    // Метод для удаления изображения из Firebase Storage
    public void deleteImage(String photoUrl, ImageUploadCallback callback) {
        if (photoUrl != null) {
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);
            photoRef.delete().addOnSuccessListener(aVoid -> {
                Log.d("FirebaseStorage", "Изображение успешно удалено из Firebase Storage");
                callback.onSuccess(null); // Вызов успеха без URL
            }).addOnFailureListener(e -> {
                Log.e("FirebaseStorage", "Ошибка при удалении изображения: " + e.getMessage());
                callback.onError(e.getMessage()); // Вызов ошибки
            });
        } else {
            callback.onError("URL изображения не предоставлен.");
        }
    }

    public interface ImageUploadCallback {
        void onSuccess(String imageUrl);
        void onError(String error);
    }
}
