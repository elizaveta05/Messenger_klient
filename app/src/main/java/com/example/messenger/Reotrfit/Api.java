package com.example.messenger.Reotrfit;

import com.example.messenger.Chat;
import com.example.messenger.Message;
import com.example.messenger.Model.Users;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;


public interface Api {

    //Метод создания профиля пользователя
    @POST("/users/createUser")
    Call<Users> registerUser(@Body Users newUser);

    //Метод получения всех логинов пользователей
    @GET("/getUsersLogin")
    Call<List<String>> getUsersLogin();

    @GET("/app/getAllChatsForUser/{senderId}")
    Call<List<Chat>> getAllChatsForUser(@Path("senderId") String senderId);
    @GET("/app/getUserById/{userId}")
    Call<Users> getUserById(@Path("userId") String userId);

    @POST("/chat/{senderId}/{recipientId}/{message}")
    Call<List<Message>> sendMessage(@Path("senderId") String senderId, @Path("recipientId") String recipientId, @Body String message);

    @GET("/app//fetchAllMessage/{senderId}/{recipientId}")
    Call<List<Message>> getAllMessage(@Path("senderId") String senderId, @Path("recipientId") String recipientId);
}