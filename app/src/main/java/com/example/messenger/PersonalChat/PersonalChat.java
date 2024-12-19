package com.example.messenger.PersonalChat;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.PersonalChat.Adapter.MessageAdapter;
import com.example.messenger.Chat.Chats;
import com.example.messenger.Model.Message;
import com.example.messenger.Model.Users;
import com.example.messenger.R;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.http.Query;

public class PersonalChat extends AppCompatActivity {
    private Users selectedUser;
    private EditText etMessage;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseUser currentUser;
    private TextView tv_name;
    private CircleImageView image_photo_user2;
    private RetrofitService retrofitService = new RetrofitService();
    private String userSend;
    private Integer chatId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_personal_chat);

        // Инициализация элементов интерфейса
        tv_name = findViewById(R.id.tv_name);
        image_photo_user2 = findViewById(R.id.image_photo_user2);
        etMessage = findViewById(R.id.et_messege);
        recyclerView = findViewById(R.id.recycler_view);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Получить текущего пользователя из Firebase
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        // Инициализация списка и адаптера
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(new ArrayList<>(), currentUser.getUid(), message -> {
            // Обработчик клика по сообщению (если нужен)
            Toast.makeText(this, "Клик по сообщению: " + message.getMessageText(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        userSend = getIntent().getStringExtra("userSend");

        // Получение id чата и истории сообщений
        if (userSend != null) {
            Log.d("ReceivedUserSend", userSend);
            getprofileUser(userSend);
            getChatId(currentUser.getUid(), userSend); // Получаем chatId и историю сообщений
        } else {
            Toast.makeText(this, "Ошибка: не удалось получить отправителя", Toast.LENGTH_SHORT).show();
        }

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
                sendMessageToServer(messageContent); // Отправка сообщения на сервер
                etMessage.setText(""); // Очищаем поле ввода после отправки
            }
        });
    }
    // Метод обновления данных для адаптера с датами
    private void updateMessageAdapter(List<Message> messages) {
        // Преобразуем список сообщений в список объектов с разделением по датам
        List<Object> formattedMessages = MessageAdapter.prepareMessageList(messages);
        // Обновляем данные в адаптере
        messageAdapter.setMessageList(formattedMessages);
        // Скроллим к последнему сообщению
        recyclerView.scrollToPosition(formattedMessages.size() - 1);
    }

    // Метод получения данных о пользователе (изображение, логин)
    private void getprofileUser(String userId) {
        Api apiService = retrofitService.getRetrofit().create(Api.class);
        Call<Users> call = apiService.getProfileUser(userId);

        call.enqueue(new Callback<Users>() {
            @Override
            public void onResponse(Call<Users> call, Response<Users> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Users user = response.body();
                    tv_name.setText(user.getLogin());
                    Picasso.get()
                            .load(user.getImage_url())
                            .placeholder(R.drawable.icon_user)
                            .error(R.drawable.icon_user)
                            .into(image_photo_user2);
                } else {
                    Toast.makeText(PersonalChat.this, "Ошибка загрузки профиля", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Users> call, Throwable t) {
                Toast.makeText(PersonalChat.this, "Ошибка сети: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод по созданию чата/отправке сообщений
    private void sendMessageToServer(String messageContent) {
        Api api = retrofitService.getRetrofit().create(Api.class);

        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedDate = now.format(formatter);

        Call<String> call = api.createChatAndSendMessage(currentUser.getUid(), userSend, messageContent, formattedDate);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Сообщение отправлено: " + response.body());
                } else {
                    Log.e(TAG, "Ошибка: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Ошибка соединения: " + t.getMessage());
            }
        });
    }
    // Метод по получению id чата
    private void getChatId(String chatUserOwner, String otherUser) {
        Api api = retrofitService.getRetrofit().create(Api.class);
        Call<String> call = api.getChatId(chatUserOwner, otherUser);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatId = Integer.valueOf(response.body());
                    Log.d(TAG, "Chat ID найден: " + chatId);
                    getMessageHistory(chatUserOwner, chatId); // Получить историю сообщений
                } else {
                    Log.e(TAG, "Чат не найден, код ответа: " + response.code());
                    Toast.makeText(PersonalChat.this, "Чат не найден", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Log.e(TAG, "Ошибка сети при получении chatId: " + t.getMessage());
                Toast.makeText(PersonalChat.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Метод по получению истории сообщений в чате
    private void getMessageHistory(String userId, Integer chatId) {
        Api api = retrofitService.getRetrofit().create(Api.class);
        Call<List<Message>> call = api.getMessageHistory(userId, chatId);

        call.enqueue(new Callback<List<Message>>() {
            @Override
            public void onResponse(Call<List<Message>> call, Response<List<Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Message> messages = response.body();
                    updateMessageAdapter(messages);
                    Log.d(TAG, "История сообщений успешно загружена: " + messages.size() + " сообщений");
                } else {
                    Log.e(TAG, "Ошибка загрузки сообщений, код ответа: " + response.code());
                    Toast.makeText(PersonalChat.this, "Ошибка загрузки сообщений", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Message>> call, Throwable t) {
                Log.e(TAG, "Ошибка сети при загрузке истории сообщений: " + t.getMessage());
                Toast.makeText(PersonalChat.this, "Ошибка сети", Toast.LENGTH_SHORT).show();
            }
        });
    }


}
