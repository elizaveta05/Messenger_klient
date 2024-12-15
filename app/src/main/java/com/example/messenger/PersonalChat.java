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

import com.example.messenger.Chat.Adapter.MessageAdapter;
import com.example.messenger.Chat.Chats;
import com.example.messenger.Model.Message;
import com.example.messenger.Model.Users;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
            overridePendingTransition(0, 0);
        });

        ImageButton btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            String messageContent = etMessage.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                sendMessageToServer(messageContent);  // Отправка сообщения на сервер
                etMessage.setText(""); // Очищаем поле ввода после отправки
            }
        });

    }
    private void sendMessageToServer(String messageContent) {
        // Инициализация сервиса Retrofit
        RetrofitService retrofitService = new RetrofitService();
        Api api = retrofitService.getRetrofit().create(Api.class);

        // Получаем текущую дату и время
        LocalDateTime now = LocalDateTime.now();

        // Форматируем дату
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);

        // Вызов метода API
        Call<String> call = api.createChatAndSendMessage(currentUser.getUid().toString(), selectedUser.getUserId().toString(), messageContent, formattedDate);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "Сообщение успешно отправлено на сервер: " + response.body());


                } else {
                    Log.e(TAG, "Ошибка при отправке сообщения: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Ошибка соединения: " + t.getMessage());
            }
        });
    }

}
