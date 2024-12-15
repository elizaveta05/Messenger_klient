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

import com.example.messenger.Chat.Adapter.RecentChatsAdapter;
import com.example.messenger.Model.RecentChats;
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
    private RecentChatsAdapter adapter; // Исправлено с RecentChats на RecentChatsAdapter
    private List<RecentChats> chatList;
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

        if (currentUser == null) {
            Toast.makeText(this, "Пользователь не авторизован.", Toast.LENGTH_SHORT).show();
            progressBar.setVisibility(View.GONE);
            return;
        }

        // Получаем экземпляр API
        Api apiService = retrofitService.getRetrofit().create(Api.class);

        // Выполняем вызов API
        Call<List<RecentChats>> call = apiService.getAllChatsForUser(currentUser.getUid());
        call.enqueue(new Callback<List<RecentChats>>() {
            @Override
            public void onResponse(Call<List<RecentChats>> call, Response<List<RecentChats>> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    chatList = response.body();

                    // Передаем данные в адаптер
                    adapter = new RecentChatsAdapter(Chats.this, chatList, chat -> {
                        // Обработка клика по чату
                        Intent intent = new Intent(Chats.this, PersonalChat.class);
                        intent.putExtra("otherUserId", chat.getUserId());
                        startActivity(intent);
                    });

                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(Chats.this, "Не удалось загрузить чаты", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecentChats>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(Chats.this, "Ошибка загрузки данных: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
