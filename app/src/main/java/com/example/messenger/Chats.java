package com.example.messenger;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Users;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chats extends AppCompatActivity {

    private ImageButton btn_profile, btn_add;
    private RecyclerView recyclerView;
    private ChatsAdapter adapter;

    private FirebaseUser currentUser;
    private String senderId;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Разрешить использование функции EdgeToEdge
        EdgeToEdge.enable(this);

        // Установить макет для этой активности
        setContentView(R.layout.activity_chats);

        // Применить настройки краевых областей для области содержимого 'main'
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Получить текущего пользователя из Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        senderId= currentUser.getUid().toString();

        progressBar = findViewById(R.id.progressBar);
        // Настроить кнопку для перехода на профиль
        btn_profile = findViewById(R.id.btn_profile);
        btn_profile.setOnClickListener(v->{
            Intent intent = new Intent(Chats.this, Profile.class);
            startActivity(intent);
            // Убрать анимацию перехода
            overridePendingTransition(0, 0);
        });

        // Настроить кнопку для добавления чата
        btn_add = findViewById(R.id.btn_add);
        btn_add.setOnClickListener(v->{
            Intent intent = new Intent(Chats.this, add_chats.class);
            startActivity(intent);
            // Убрать анимацию перехода
            overridePendingTransition(0, 0);
        });

        // Настроить RecyclerView для отображения списка чатов
        recyclerView = findViewById(R.id.allContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        if(senderId != null) {
            // Установить соединение по WebSocket
            connectWebSocket(senderId);
        }

    }

    // Метод для установки соединения по WebSocket
    private void connectWebSocket(String senderId) {
        RetrofitService retrofitService = new RetrofitService();
        Api api = retrofitService.getRetrofit().create(Api.class);
        api.getAllChatsForUser(senderId).enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Request successful");
                    Gson gson = new Gson();
                    String chatListJson = gson.toJson(response.body());
                    setupRecyclerView(chatListJson);
                } else {
                    try {
                        Log.e(TAG, "Request failed. Error message: " + response.errorBody().string());
                        Log.e(TAG, "Response code: " + response.code());
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Chat>> call, Throwable throwable) {
                if (throwable instanceof IOException) {
                    Log.e(TAG, "Network error", throwable);
                } else {
                    Log.e(TAG, "Request failed", throwable);
                }
            }
        });
    }
    private void setupRecyclerView(String chatsJson) {
        Gson gson = new Gson();
        Type chatListType = new TypeToken<List<Chat>>() {}.getType();
        List<Chat> chatList = gson.fromJson(chatsJson, chatListType);

        adapter = new ChatsAdapter(this, chatList, chat -> {
            // Обращение к серверу для получения данных выбранного пользователя
            fetchUserDataFromServer(chat.getUserId());
        });

        // Скрыть progressBar
        progressBar.setVisibility(View.GONE);

        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();  // Обновляем данные в списке
    }
    private void fetchUserDataFromServer(String userId) {
        RetrofitService retrofitService = new RetrofitService();
        Api api = retrofitService.getRetrofit().create(Api.class);
        api.getUserById(userId).enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Users selectedUser = response.body();

                    Intent intent = new Intent(Chats.this, PersonalChat.class);
                    intent.putExtra("selectedUser", selectedUser);
                    startActivity(intent);
                } else {
                    Toast.makeText(Chats.this, "Failed to get user data", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<Users> call, Throwable throwable) {
                Toast.makeText(Chats.this, "Error fetching user data", Toast.LENGTH_SHORT).show();
            }
        });
    }
}