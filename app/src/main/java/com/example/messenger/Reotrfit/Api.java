package com.example.messenger.Reotrfit;

import com.example.messenger.Model.Chat;
import com.example.messenger.Model.Message;
import com.example.messenger.Model.RecentChats;
import com.example.messenger.Model.Users;

import java.util.List;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


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

    //Метод изменения данных профиля конкретного пользователя
    @PUT("/users/updateUserProfile/{userId}")
    Call<Users> updateUserProfile(@Path("userId") String userId, @Body Users user);

    //Метод проверки списка контактов
    @POST("/search/contacts")
    Call<List<Users>> searchContacts(@Body List<String> contacts);

    //Метод по созданию чата/сохранению сообщения
    @FormUrlEncoded
    @POST("/chats/createChatAndSendMessage")
    Call<String> createChatAndSendMessage(
            @Field("chatUserOwner") String chatUserOwner,
            @Field("otherUser") String otherUser,
            @Field("messageText") String messageText,
            @Field("timeCreated") String timeCreated
    );
    // Метод получения всех чатов для пользователя
    @GET("/chats/getAllChatsForUser")
    Call<List<RecentChats>> getAllChatsForUser(@Query("userId") String userId);

    @GET("/search/by-login")
    Call<List<Users>> searchByLogin(@Query("query") String query);

    @DELETE("/deleteChat")
    Call<ResponseBody> deleteChat(@Query("userId") String userId, @Query("chatId") Integer chatId);

    @GET("/app/getUserById/{userId}")
    Call<Users> getUserById(@Path("userId") String userId);

    @POST("/chat/{senderId}/{recipientId}/{message}")
    Call<List<Message>> sendMessage(@Path("senderId") String senderId, @Path("recipientId") String recipientId, @Body String message);

    @GET("/app//fetchAllMessage/{senderId}/{recipientId}")
    Call<List<Message>> getAllMessage(@Path("senderId") String senderId, @Path("recipientId") String recipientId);
}