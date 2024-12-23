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

import com.example.messenger.Model.Messages;
import com.example.messenger.PersonalChat.Adapter.MessageAdapter;
import com.example.messenger.Chat.Chats;
import com.example.messenger.Model.Users;
import com.example.messenger.R;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PersonalChat extends AppCompatActivity {
    private Users selectedUser;
    private EditText etMessage;
    private RecyclerView recyclerView;
    private MessageAdapter messageAdapter;
    private List<Messages> messageList;
    private FirebaseUser currentUser;
    private TextView tv_name;
    private CircleImageView image_photo_user2;
    private RetrofitService retrofitService = new RetrofitService();
    private String userSend;
    private Integer chatId;
    private WebSocketManager webSocketManager;
    private static final String WS_URL = "ws://192.168.0.1:8080/chat";


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
            Toast.makeText(this, "Клик по сообщению: " + message.getMessageText(), Toast.LENGTH_SHORT).show();
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(messageAdapter);

        userSend = getIntent().getStringExtra("userSend");

        if (userSend != null) {
            Log.d("ReceivedUserSend", userSend);
            getprofileUser(userSend);
            getChatId(currentUser.getUid(), userSend);
        } else {
            Toast.makeText(this, "Ошибка: не удалось получить отправителя", Toast.LENGTH_SHORT).show();
        }

        ImageButton btn_back = findViewById(R.id.btn_back);
        btn_back.setOnClickListener(v -> {
            Intent intent = new Intent(PersonalChat.this, Chats.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
        });

        // Инициализация WebSocketManager
        webSocketManager = new WebSocketManager(WS_URL);
        webSocketManager.connect();

        ImageButton btnSend = findViewById(R.id.btn_send);
        btnSend.setOnClickListener(v -> {
            String messageContent = etMessage.getText().toString().trim();
            if (!messageContent.isEmpty()) {
                //webSocketManager.sendMessage(messageContent);
                sendMessageToServer(messageContent);
                etMessage.setText("");
            }
        });
    }
    private void sendMessageToServer(String messageContent) {
        Api api = retrofitService.getRetrofit().create(Api.class);

        // Разделение сообщения на части, если его длина превышает 255 символов
        int maxLength = 255;
        int messageLength = messageContent.length();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        if (messageLength > maxLength) {
            int partCount = (int) Math.ceil((double) messageLength / maxLength); // Количество частей

            for (int i = 0; i < partCount; i++) {
                int start = i * maxLength;
                int end = Math.min(start + maxLength, messageLength);
                String messagePart = messageContent.substring(start, end);

                // Отправка текущей части сообщения
                sendSingleMessage(api, messagePart, now.format(formatter));
            }
        } else {
            // Если сообщение помещается в один запрос
            sendSingleMessage(api, messageContent, now.format(formatter));
        }
    }

    private void updateMessageAdapter(List<Messages> messages) {
        messageAdapter.setMessageList(messages);
        recyclerView.scrollToPosition(messages.size() - 1);
    }

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

    private void sendSingleMessage(Api api, String messageContent, String formattedDate) {
        // Отправка через WebSocket
        webSocketManager.sendMessage(createJsonMessage(currentUser.getUid(), userSend, messageContent, formattedDate));
    }

    private String createJsonMessage(String userId, String userSendId, String messageContent, String formattedDate) {
        // Создаем JSON-структуру для отправки через WebSocket
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("chatUserOwner", userId);
            jsonObject.put("otherUser", userSendId);
            jsonObject.put("messageText", messageContent);
            jsonObject.put("timeCreated", formattedDate);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }


    private void getChatId(String chatUserOwner, String otherUser) {
        Api api = retrofitService.getRetrofit().create(Api.class);
        Call<String> call = api.getChatId(chatUserOwner, otherUser);

        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.isSuccessful() && response.body() != null) {
                    chatId = Integer.valueOf(response.body());
                    Log.d(TAG, "Chat ID найден: " + chatId);
                    getMessageHistory(chatUserOwner, chatId);
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

    private void getMessageHistory(String userId, Integer chatId) {
        Api api = retrofitService.getRetrofit().create(Api.class);
        Call<List<Messages>> call = api.getMessageHistory(userId, chatId);

        call.enqueue(new Callback<List<Messages>>() {
            @Override
            public void onResponse(Call<List<Messages>> call, Response<List<Messages>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Messages> messages = response.body();
                    for (Messages message : messages) {
                        Log.d(TAG, "Сообщение: " + message.getMessageText());
                    }
                    updateMessageAdapter(messages);
                } else {
                    Log.e(TAG, "Ошибка ответа: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Messages>> call, Throwable t) {
                Log.e(TAG, "Ошибка: " + t.getMessage());
            }
        });
    }
}
