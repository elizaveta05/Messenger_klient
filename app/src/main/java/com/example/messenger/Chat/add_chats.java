package com.example.messenger.Chat;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Bundle;
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
import com.example.messenger.UserAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;

public class add_chats extends AppCompatActivity implements UserAdapter.OnUserClickListener {
    private ArrayList<Users> userList = new ArrayList<>();
    private UserAdapter userAdapter;
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
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().toLowerCase().trim(); // Преобразуем в нижний регистр и убираем пробелы
            }

            @Override
            public void afterTextChanged(Editable s) {
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
    public void accessToContacts() {
        // Проверяем, было ли предоставлено разрешение на доступ к контактам
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            // Если разрешение не предоставлено, запрашиваем его у пользователя
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.READ_CONTACTS}, 1);
        } else {
            // Разрешение уже предоставлено, можно получить контакты
            getContacts();
        }
    }

    // Обработчик результата запроса разрешения
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // Вызываем метод суперкласса для выполнения стандартной обработки
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // Проверяем, соответствует ли запрос разрешения, который мы сделали
        if (requestCode == 1) {
            // Если пользователь предоставил разрешение
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Получаем контакты
                getContacts();
            } else {
                // Если разрешение отклонено, показываем сообщение пользователю
                Toast.makeText(this, "Доступ к контактам отклонен", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Метод для получения списка контактов
    private void getContacts() {
        // Создаем список для хранения контактов
        ArrayList<String> contactsList = new ArrayList<>();
        // Выполняем запрос к содержимому (контент-провайдеру) для получения данных о контактах
        Cursor cursor = getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                null, null, null, null); // Получаем все номера телефонов

        if (cursor != null) {
            // Получаем индексы для имени и номера телефона из курсора
            int nameIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
            int phoneIndex = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);

            // Проходим по всем записям в курсоре
            while (cursor.moveToNext()) {
                // Проверяем, что индексы не равны -1, что означает, что соответствующие столбцы существуют
                if (nameIndex != -1 && phoneIndex != -1) {
                    // Извлекаем имя и номер телефона из текущей записи
                    String name = cursor.getString(nameIndex);
                    String phoneNumber = cursor.getString(phoneIndex);
                    // Добавляем контакт в список в формате "Имя: Номер"
                    contactsList.add(name + ": " + phoneNumber);
                }
            }
            // Закрываем курсор для освобождения ресурсов
            cursor.close();
        }

        // Для каждого контакта в списке выводим его в лог
        for (String contact : contactsList) {
            Log.d(TAG, "Contact: " + contact);
        }
    }

}