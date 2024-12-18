package com.example.messenger;

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

import com.example.messenger.Chat.Adapter.MessageAdapter;
import com.example.messenger.Chat.Chats;
import com.example.messenger.Model.Message;
import com.example.messenger.Model.RecentChats;
import com.example.messenger.Model.Users;
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

        // Получение объекта RecentChats
        userSend = getIntent().getStringExtra("userSend");
        if (userSend != null) {
            Log.d("ReceivedUserSend", userSend);
            getprofileUser(userSend);
        } else {
            Toast.makeText(this, "Ошибка: не удалось получить отправителя", Toast.LENGTH_SHORT).show();
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
                sendMessageToServer(messageContent); // Отправка сообщения на сервер
                etMessage.setText(""); // Очищаем поле ввода после отправки
            }
        });
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

}
