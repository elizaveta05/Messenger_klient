package com.example.messenger;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.reotrfit.Api;
import com.example.messenger.reotrfit.RetrofitService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalChat extends AppCompatActivity {
    private Users selectedUser;
    private EditText etMessage;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseUser currentUser;
    private String senderId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_chat);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Получить текущего пользователя из Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        senderId= currentUser.getUid().toString();
        selectedUser = getIntent().getParcelableExtra("selectedUser");
        etMessage = findViewById(R.id.et_messege);
        recyclerView = findViewById(R.id.recycler_view);

        if (selectedUser != null) {
            TextView tvName = findViewById(R.id.tv_name);
            tvName.setText(selectedUser.getLogin());
        }

        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, currentUser.getUid());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        ImageButton btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalChat.this, Chats.class);
            startActivity(intent);
            // Убрать анимацию перехода
            overridePendingTransition(0, 0);
        });

        ImageButton btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            String messageContent = etMessage.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessageToServer(messageContent, selectedUser.getUserId());  // Отправка сообщения на сервер
                etMessage.setText(""); // Очищаем поле ввода после отправки
            }
        });
        if (senderId != null) {
            connectWebSocket(senderId, selectedUser.getUserId());
        }
    }
    // Метод для установки соединения по WebSocket
    private void connectWebSocket(String senderId, String recipientId) {
        RetrofitService retrofitService = new RetrofitService();
        Api api = retrofitService.getRetrofit().create(Api.class);
        api.getAllMessage(senderId, recipientId).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful()) {
                    Log.i(TAG, "Request successful");
                    List<Message> messages = response.body();
                    if (messages != null) {
                        setupRecyclerView(messages);
                    } else {
                        Log.e(TAG, "Received null message list from server");
                    }
                } else {
                    Log.e(TAG, "Request failed: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable throwable) {
                Log.e(TAG, "Request failed: " + throwable.getMessage());
            }
        });
    }

    private void setupRecyclerView(List<Message> messages) {
        messageList.clear();
        messageList.addAll(messages);

        if (messageAdapter == null) {
            messageAdapter = new MessageAdapter(messageList, currentUser.getUid());
            recyclerView.setAdapter(messageAdapter);
        } else {
            messageAdapter.notifyDataSetChanged();
        }

        if (messageList.size() > 0) {
            recyclerView.smoothScrollToPosition(messageList.size() - 1);
        }
    }
    private void sendMessageToServer(String message, String recipientId) {
        // Создание экземпляра RetrofitService
        RetrofitService retrofitService = new RetrofitService();
        // Получение экземпляра Api через Retrofit
        Api api = retrofitService.getRetrofit().create(Api.class);
        // Отправка сообщения на сервер с использованием Retrofit
        api.sendMessage(currentUser.getUid(), recipientId, message).enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {

                    messageList.addAll(response.body());

                    recyclerView.post(() -> {
                        messageAdapter.notifyDataSetChanged();
                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                    });
                } else {
                    Log.e(TAG, "Запрос не удался: " + response.message());
                }
            }
            @Override
            public void onFailure(Call<List<Message>> call, Throwable throwable) {
                // Логирование сообщения об ошибке в случае сбоя запроса
                Log.e(TAG, "Не удалось выполнить запрос: " + throwable.getMessage());
            }
        });
    }
}
