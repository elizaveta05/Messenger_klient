package com.example.messenger.Reotrfit;

import com.example.messenger.Chat;
import com.example.messenger.Message;
import com.example.messenger.Model.Users;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;


public interface Api {

    // Метод создания профиля пользователя с изображением
    @Multipart
    @POST("/users/createUser")
    Call<Users> registerUser(
            @Part("userId") RequestBody userId,
            @Part("login") RequestBody login,
            @Part("phoneNumber") RequestBody phoneNumber,
            @Part("image_url") RequestBody image_url
    );

    // Метод получения всех логинов пользователей
    @GET("/users/getUsersLogin")
    Call<List<String>> getUsersLogin();

    // Метод получения данных профиля для конкретного пользователя
    @GET("/users/getProfileUser/{userId}")
    Call<Users> getProfileUser(@Path("userId") String userId);

    //Метод проверки на нахождения телефона в бд
    @GET("/users/getPhoneNumber/{phoneNumber}")
    Call<Boolean> getPhoneNumber(@Path("phoneNumber") String phoneNumber);

    // Метод удаления профиля пользователя
    @DELETE("/users/deleteProfileUser/{userId}")
    Call<String> deleteProfileUser(@Path("userId") String userId);

    @GET("/app/getAllChatsForUser/{senderId}")
    Call<List<Chat>> getAllChatsForUser(@Path("senderId") String senderId);
    @GET("/app/getUserById/{userId}")
    Call<Users> getUserById(@Path("userId") String userId);

    @POST("/chat/{senderId}/{recipientId}/{message}")
    Call<List<Message>> sendMessage(@Path("senderId") String senderId, @Path("recipientId") String recipientId, @Body String message);

    @GET("/app//fetchAllMessage/{senderId}/{recipientId}")
    Call<List<Message>> getAllMessage(@Path("senderId") String senderId, @Path("recipientId") String recipientId);
}