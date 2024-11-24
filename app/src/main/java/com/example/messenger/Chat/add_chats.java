package com.example.messenger.Chat;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import com.example.messenger.Model.Users;
import com.example.messenger.PersonalChat;
import com.example.messenger.Profile;
import com.example.messenger.R;
import com.example.messenger.Reotrfit.Api;
import com.example.messenger.Reotrfit.RetrofitService;
import com.example.messenger.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class add_chats extends AppCompatActivity implements UserAdapter.OnUserClickListener {
    private ArrayList<Users> userList = new ArrayList<>();
    private UserAdapter userAdapter;
    private FirebaseUser currentUser;
    private Users user;
    private RetrofitService retrofitService = new RetrofitService();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_chats);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Проверка разрешений и получение контактов
        accessToContacts();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        ImageButton btn_chat = findViewById(R.id.btn_chat);
        btn_chat.setOnClickListener(v->{
            Intent intent = new Intent(add_chats.this, Chats.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Убрать анимацию перехода
        });

        ImageButton btn_profile = findViewById(R.id.btn_profile);
        btn_profile.setOnClickListener(v->{
            Intent intent = new Intent(add_chats.this, Profile.class);
            startActivity(intent);
            overridePendingTransition(0, 0); // Убрать анимацию перехода
        });

        RecyclerView recyclerView = findViewById(R.id.allContacts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        userAdapter = new UserAdapter(this, userList, this);
        recyclerView.setAdapter(userAdapter);

        EditText searchUsers = findViewById(R.id.search_users);
        searchUsers.setSingleLine(); // Устанавливаем одну строку для EditText
        searchUsers.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacksAndMessages(null); // Отменяем предыдущий запрос
                String query = s.toString().trim();
                if(query.length() >=3) {
                    if (!query.isEmpty()) {
                        handler.postDelayed(() -> fetchUsersByLogin(query), 300); // Задержка 300 мс
                    } else {
                        userList.clear();
                        userAdapter.notifyDataSetChanged(); // Очищаем список при пустом вводе
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    @Override
    public void onUserClick(Users user) {
        if (user.getUserId() != null && currentUser != null && currentUser.getUid() != null) {
            if (currentUser.getUid().equals(user.getUserId())) {
                Toast.makeText(this, "Это вы!!!", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(add_chats.this, PersonalChat.class);
                intent.putExtra("selectedUser", user);
                startActivity(intent);
            }
        } else {
            Log.e(TAG, "User ID or currentUser is null");
        }
    }
    public void accessToContacts() {
        // Проверяем разрешение
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Если разрешение не предоставлено, запрашиваем его у пользователя
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS}, 1);
        } else {
            // Даже если разрешение уже предоставлено, всё равно получаем контакты
            getContacts();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1 && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getContacts();
        } else {
            Toast.makeText(this, "Доступ к контактам отклонен", Toast.LENGTH_SHORT).show();
        }
    }

    private void getContacts() {
        ArrayList<String> contactsList = new ArrayList<>();
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null);

        if (cursor != null) {
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
            while (cursor.moveToNext()) {
                if (phoneIndex != -1) {
                    String phoneNumber = cursor.getString(phoneIndex).replaceAll("\\s+", "");
                    contactsList.add(phoneNumber);
                }
            }
            cursor.close();
        }

        // Отправляем номера на сервер для получения профилей
        fetchProfilesFromServer(contactsList);
    }

    private void fetchProfilesFromServer(ArrayList<String> contactsList) {

        Api apiService = retrofitService.getRetrofit().create(Api.class);
        Call<List<Users>> call = apiService.searchContacts(contactsList);

        call.enqueue(new Callback<List<Users>>() {
            @Override
            public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    userList.clear();
                    userList.addAll(response.body());
                    userAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(add_chats.this, "Ошибка загрузки данных", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Users>> call, Throwable t) {
                Log.e("add_chats", "Ошибка: " + t.getMessage());
                Toast.makeText(add_chats.this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUsersByLogin(String query) {
        Api apiService = retrofitService.getRetrofit().create(Api.class);
        Call<List<Users>> call = apiService.searchByLogin(query);

        call.enqueue(new Callback<List<Users>>() {
            @Override
            public void onResponse(Call<List<Users>> call, Response<List<Users>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Фильтруем полученные профили, исключая текущего пользователя
                    String currentUserId = currentUser != null ? currentUser.getUid() : null;
                    List<Users> filteredUsers = response.body().stream()
                            .filter(user -> !user.getUserId().equals(currentUserId))
                            .collect(Collectors.toList());

                    userList.clear();
                    userList.addAll(filteredUsers);
                    userAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(add_chats.this, "Пользователи не найдены", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Users>> call, Throwable t) {
                Log.e("add_chats", "Ошибка: " + t.getMessage());
                Toast.makeText(add_chats.this, "Ошибка подключения", Toast.LENGTH_SHORT).show();
            }
        });
    }

}