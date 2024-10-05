package com.example.messenger;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.messenger.Model.Users;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class add_chats extends AppCompatActivity implements UserAdapter.OnUserClickListener{
    private ArrayList<Users> userList = new ArrayList<>();
    private UserAdapter userAdapter;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser currentUser;
    private Users user;


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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim(); // Преобразуем в нижний регистр и убираем пробелы
                dynamicSearchUsers(query);
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
    }
    private void dynamicSearchUsers(String query) {
        db.collection("Users")
                .whereGreaterThanOrEqualTo("login", query)
                .whereLessThanOrEqualTo("login", query + "\uf8ff") // Используем маркер для динамического поиска
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        ArrayList<Users> updatedUserList = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            user = document.toObject(Users.class);
                            user.setUserId(document.getId());
                            updatedUserList.add(user);
                        }
                        userList.clear();
                        userList.addAll(updatedUserList);
                        userAdapter.setUserList(userList);
                        userAdapter.notifyDataSetChanged();
                    } else {
                        Log.d(TAG, "Ошибка при получении документов: ", task.getException());
                    }
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
}