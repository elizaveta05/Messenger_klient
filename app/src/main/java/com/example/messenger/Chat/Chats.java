package com.example.messenger.Chat;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
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
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Chats extends AppCompatActivity {

    private ImageButton btn_profile, btn_add;
    private RecyclerView recyclerView;
    private RecentChatsAdapter adapter;
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
                    adapter = new RecentChatsAdapter(Chats.this, chatList, chat -> {
                        Intent intent = new Intent(Chats.this, PersonalChat.class);
                        intent.putExtra("userSend", chat.getUserSend());
                        startActivity(intent);
                    }, (view, chat) -> {
                        showBottomSheetMenu(chat);
                    });



                    recyclerView.setAdapter(adapter);
                } else {
                    Toast.makeText(Chats.this, "Не удалось загрузить чаты", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<RecentChats>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("ChatsActivity", "Ошибка загрузки данных", t);
                Toast.makeText(Chats.this, "Ошибка загрузки данных: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void showBottomSheetMenu(RecentChats chat) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_menu, null);
        bottomSheetDialog.setContentView(sheetView);

        sheetView.findViewById(R.id.menu_archive).setOnClickListener(v -> {
            archiveChat(chat);
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.menu_delete).setOnClickListener(v -> {
            deleteChat(chat);
            bottomSheetDialog.dismiss();
        });

        sheetView.findViewById(R.id.menu_block).setOnClickListener(v -> {
            blockUser(chat);
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    private void archiveChat(RecentChats chat) {
        // Логика для архивирования чата
        Toast.makeText(this, "Чат архивирован: " + chat.getLogin(), Toast.LENGTH_SHORT).show();
    }

    private void deleteChat(RecentChats chat) {
        // Убираем чат из списка для визуального эффекта
        chatList.remove(chat);
        adapter.notifyDataSetChanged();

        // Показываем Snackbar с кнопкой отмены
        Snackbar snackbar = Snackbar.make(recyclerView, "Чат удалён", Snackbar.LENGTH_LONG);
        snackbar.setAction("Отменить", v -> {
            // Восстановить чат в списке
            chatList.add(chat);
            adapter.notifyDataSetChanged();
            Toast.makeText(this, "Удаление отменено", Toast.LENGTH_SHORT).show();
        });

        snackbar.addCallback(new Snackbar.Callback() {
            @Override
            public void onDismissed(Snackbar transientBottomBar, int event) {
                if (event != Snackbar.Callback.DISMISS_EVENT_ACTION) {
                    // Если пользователь не нажал "Отменить", удаляем чат через API
                    performChatDeletion(chat);
                }
            }
        });

        snackbar.show();
    }

    // Метод для вызова API удаления чата
    private void performChatDeletion(RecentChats chat) {
        progressBar.setVisibility(View.VISIBLE);

        Api apiService = retrofitService.getRetrofit().create(Api.class);
        Call<ResponseBody> call = apiService.deleteChat(currentUser.getUid(), chat.getChatId());
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);

                if (response.isSuccessful()) {
                    Toast.makeText(Chats.this, "Чат успешно удалён.", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(Chats.this, "Ошибка удаления чата: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Log.e("ChatsActivity", "Ошибка удаления чата", t);
                Toast.makeText(Chats.this, "Ошибка удаления чата: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockUser(RecentChats chat) {
        // Логика для блокировки пользователя
        Toast.makeText(this, "Пользователь заблокирован: " + chat.getLogin(), Toast.LENGTH_SHORT).show();
    }

}
