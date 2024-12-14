package com.example.messenger.Chat;

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

import com.example.messenger.Chat.Adapter.ChatsAdapter;
import com.example.messenger.Model.Chat;
import com.example.messenger.PersonalChat;
import com.example.messenger.Profile;
import com.example.messenger.R;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chats extends AppCompatActivity {

    private ImageButton btn_profile, btn_add;
    private RecyclerView recyclerView;
    private ChatsAdapter adapter;
    private List<Chat> chatList;
    private FirebaseUser currentUser;
    private ProgressBar progressBar;
    private RetrofitService retrofitService = new RetrofitService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chats);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        progressBar = findViewById(R.id.progressBar);
        btn_profile = findViewById(R.id.btn_profile);
        btn_add = findViewById(R.id.btn_add);

        btn_profile.setOnClickListener(v -> {
            startActivity(new Intent(Chats.this, Profile.class));
            overridePendingTransition(0, 0);
        });

        btn_add.setOnClickListener(v -> {
            startActivity(new Intent(Chats.this, add_chats.class));
            overridePendingTransition(0, 0);
        });

        recyclerView = findViewById(R.id.allContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Загружаем чаты для текущего пользователя
        loadChats();
    }

    // Метод для загрузки чатов
    private void loadChats() {
        progressBar.setVisibility(View.VISIBLE);
        Api apiService = retrofitService.getRetrofit().create(Api.class);

        Call<List<Chat>> call = apiService.getAllChatsForUser(currentUser.getUid());
        call.enqueue(new Callback<List<Chat>>() {
            @Override
            public void onResponse(Call<List<Chat>> call, Response<List<Chat>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    chatList = response.body();
                    adapter = new ChatsAdapter(Chats.this, chatList, chat -> {
                        Intent intent = new Intent(Chats.this, PersonalChat.class);
                        intent.putExtra("chatId", chat.getChatId());
                        startActivity(intent);
                    });
                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(Chats.this, "Нет доступных чатов", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Chat>> call, Throwable t) {
                progressBar.setVisibility(View.GONE); // Скрываем прогресс бар
                Log.e(TAG, "Ошибка при загрузке чатов: " + t.getMessage());
                Toast.makeText(Chats.this, "Ошибка при загрузке чатов", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
