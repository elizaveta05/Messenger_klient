package com.example.messenger;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.messenger.authentication.MainActivity;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile extends AppCompatActivity {

    private ImageButton btn_function_list, btn_add_photo, btn_change, btn_save, btn_chat,btn_add;
    private TextInputEditText et_phoneNumber;
    private EditText et_login;
    private CircleImageView image_photo;
    private Bitmap selectedImageBitmap;
    private FirebaseUser currentUser;
    private FirebaseAuth mAuth;
    private String photoUrlBD, loginBD, phoneNumber, newphotoUrl, newLogin;
    private TextInputLayout textInputLayoutLogin;
    private boolean isChange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        isChange = false;
        mAuth = FirebaseAuth.getInstance();
        btn_function_list = findViewById(R.id.btn_function_list);
        btn_function_list.setOnClickListener(v -> {
            String[] actions = {"Выйти из аккаунта", "Удалить аккаунт", "Заблокированные контакты"};

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(Profile.this, android.R.layout.simple_list_item_1, actions) {
                @NonNull
                @Override
                public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                    TextView textView = (TextView) super.getView(position, convertView, parent);
                    textView.setTextColor(Color.BLACK);

                    return textView;
                }
            };

            // Создаем AlertDialog и устанавливаем список действий
            AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
            builder.setTitle(Html.fromHtml("<font color=\"" + R.color.white + "\">Список действий</font>"));
            builder.setIcon(R.drawable.icon);
            builder.setAdapter(adapter, (dialog, which) -> {
                switch (which) {
                    case 0:
                        FirebaseAuth.getInstance().signOut();
                        Intent intent = new Intent(Profile.this, MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                        break;
                    case 1:
                        showConfirmationDialog();
                        break;
                    case 2:
                        // "Заблокированные контакты"
                        break;
                }
            });

            // Установка цвета фона на белый
            AlertDialog alertDialog = builder.create();
            alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

            // Отображаем AlertDialog
            alertDialog.show();
        });


        btn_add_photo = findViewById(R.id.btn_add_photo);
        btn_add_photo.setOnClickListener(v->{
            if(isChange == true){
                Toast.makeText(Profile.this, "Завершите изменения и сохраните их!", Toast.LENGTH_SHORT).show();
            }else if (isChange == false){
                // Создаем AlertDialog
                AlertDialog.Builder builder = new AlertDialog.Builder(Profile.this);
                builder.setTitle("Выберите действие");
                // Цвет текста сообщения
                int blackColor = Color.BLACK;
                builder.setMessage(Html.fromHtml("<font color='" + blackColor + "'>Хотите изменить фото или удалить текущее изображение</font>"));
                builder.setPositiveButton("Изменить", (dialog, which) -> requestPermissionLauncher.launch("image/*"));
                builder.setNegativeButton("Удалить", (dialog, which) -> {
                    deletePhotoFromStorage(photoUrlBD);
                    saveUserDataToFirestore(currentUser, loginBD, null);
                    image_photo.setImageResource(R.drawable.icon_user);
                });

                // Сохраняем AlertDialog для последующей настройки
                AlertDialog alertDialog = builder.create();

                // Настройка оформления AlertDialog
                Window window = alertDialog.getWindow();
                if (window != null) {
                    window.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
                }
                // Цвет текста заголовка
                int color1 = getResources().getColor(R.color.color1);
                alertDialog.setTitle(Html.fromHtml("<font color='" + color1 + "'>Выберите действие</font>"));
                // Цвет кнопок
                int color3 = getResources().getColor(R.color.color3);
                int color5 = getResources().getColor(R.color.color1);
                alertDialog.setOnShowListener(dialogInterface -> {
                    Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                    positiveButton.setTextColor(color3);

                    Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
                    negativeButton.setTextColor(color5);
                });

                // Отображаем AlertDialog
                alertDialog.show();
            }
        });
        et_phoneNumber=findViewById(R.id.et_phoneNumber);
        et_phoneNumber.setEnabled(false);
        textInputLayoutLogin = findViewById(R.id.textInputLayoutLogin);
        et_login = textInputLayoutLogin.getEditText();
        et_login.setEnabled(false);

        if (et_login != null) {
            et_login.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    newLogin = s.toString();
                    boolean isValid = isValidLogin(newLogin);

                    if (isValid) {
                        textInputLayoutLogin.setBoxStrokeColor(getResources().getColor(R.color.green));
                    } else {
                        textInputLayoutLogin.setBoxStrokeColor(getResources().getColor(R.color.red));
                    }
                }
            });
        }
        btn_save = findViewById(R.id.btn_save);
        btn_save.setVisibility(View.INVISIBLE);
        btn_save.setOnClickListener(v -> {
            saveUserDataToFirestore(currentUser, newLogin, photoUrlBD);
            et_login.setEnabled(false);
            loginBD=newLogin;
            isChange=false;
        });
        btn_change = findViewById(R.id.btn_change);
        btn_change.setOnClickListener(v -> {
            final Toast toast = Toast.makeText(getApplicationContext(), "Логин должен содержать символы a-z, 0-9, подчеркивание и не содержать пробелы." +
                    "Минимальная длина 5 символов.", Toast.LENGTH_SHORT);
            toast.show();
            isChange = true;
            et_login.setEnabled(true);
            et_login.requestFocus();
            btn_save.setVisibility(View.VISIBLE);
        });
        btn_chat = findViewById(R.id.btn_chat);
        btn_chat.setOnClickListener(v->{
            Intent intent = new Intent(Profile.this, Chats.class);
            startActivity(intent);
            overridePendingTransition(0, 0);

        });
        btn_add = findViewById(R.id.btn_add);
        image_photo = findViewById(R.id.image_photo_user);
        loadUserDataFromFirestore();

    }
    private void loadUserDataFromFirestore() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            DocumentReference userRef = db.collection("Users").document(currentUser.getUid());
            userRef.get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            loginBD = documentSnapshot.getString("login");
                            phoneNumber = documentSnapshot.getString("phoneNumber");
                            photoUrlBD = documentSnapshot.getString("photoUrl");

                            et_login.setText(loginBD);
                            et_phoneNumber.setText(phoneNumber);

                            Picasso.get()
                                    .load(photoUrlBD)
                                    .placeholder(R.drawable.icon_user)
                                    .error(R.drawable.icon_user)
                                    .into(image_photo);
                        } else {
                            Toast.makeText(Profile.this, "Данные пользователя не найдены", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Ошибка при загрузке данных пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private boolean isValidLogin(String login) {
        if (TextUtils.isEmpty(login)) {
            return false;
        }

        if (login.length() < 5) {
            return false;
        }

        if (!login.matches("[a-zA-Z0-9_]+")) {
            return false;
        }

        if (login.contains(" ")) {
            return false;
        }

        return true;
    }
    private void saveUserDataToFirestore(FirebaseUser user, String login, String photoUrl) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> userData = new HashMap<>();
        userData.put("login", login);
        userData.put("phoneNumber", phoneNumber);
        userData.put("photoUrl", photoUrl);

        db.collection("Users")
                .document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(Profile.this, "Данные успешно изменены", Toast.LENGTH_SHORT).show();

                })
                .addOnFailureListener(e -> Toast.makeText(Profile.this, "Ошибка записи данных пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
    ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.GetContent(),
            new ActivityResultCallback<Uri>() {
                @Override
                public void onActivityResult(Uri result) {
                    try {
                        selectedImageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), result);
                        image_photo.setImageBitmap(selectedImageBitmap);
                        if (photoUrlBD != null){
                            deletePhotoFromStorage(photoUrlBD);
                        }
                        uploadPhotoToStorage(currentUser);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Изображение не выбрано!", Toast.LENGTH_SHORT).show();
                    }
                }
            });
    private void uploadPhotoToStorage(FirebaseUser user) {
        if (selectedImageBitmap != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("Photo_profile").child(user.getUid() + ".jpg");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedImageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            UploadTask uploadTask = storageRef.putBytes(data);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    newphotoUrl = uri.toString();
                    saveUserDataToFirestore(user, loginBD, newphotoUrl);
                    photoUrlBD=newphotoUrl;
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(Profile.this, "Ошибка загрузки изображения", Toast.LENGTH_SHORT).show();
            });
        } else {
            saveUserDataToFirestore(user, loginBD, null);
        }
    }
    private void deletePhotoFromStorage(String photoUrl) {
        if (photoUrl != null) {
            // Получить ссылку на файл, который нужно удалить
            StorageReference photoRef = FirebaseStorage.getInstance().getReferenceFromUrl(photoUrl);

            // Удалить файл из Firebase Storage
            photoRef.delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(Profile.this, "Изображение удалено!", Toast.LENGTH_SHORT).show();
                        photoUrlBD = null;
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(Profile.this, "Ошибка при удалении старого изображения из хранилища: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        }
    }
    private void showConfirmationDialog() {
        AlertDialog.Builder confirmationDialog = new AlertDialog.Builder(Profile.this);

        int color1 = getResources().getColor(R.color.color1);
        int colorBlack = Color.BLACK;
        int colorGrey = Color.GREEN;
        int colorRed = Color.RED;

        confirmationDialog.setTitle(Html.fromHtml("<font color='" + color1 + "'>Удаление аккаунта</font>"));
        confirmationDialog.setMessage(Html.fromHtml("<font color='" + colorBlack + "'>Вы уверены, что хотите удалить аккаунт?</font>"));

        confirmationDialog.setPositiveButton("Да", (dialog, which) -> {
            deleteAccount();
        });
        confirmationDialog.setNegativeButton("Нет", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog alertDialog = confirmationDialog.create();
        alertDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));

        alertDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE);
            positiveButton.setTextColor(colorRed);

            Button negativeButton = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE);
            negativeButton.setTextColor(colorGrey);
        });
        alertDialog.show();
    }
    private void deleteAccount() {
        // Удаляем изображение из хранилища Firebase
        if (photoUrlBD != null){
            deletePhotoFromStorage(photoUrlBD);
        }

        // Удаляем запись пользователя из Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("Users")
                .document(currentUser.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> {
                    // Удаляем аккаунт из аутентификации Firebase
                    currentUser.delete()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(Profile.this, "Аккаунт успешно удален", Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(Profile.this, MainActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    Toast.makeText(Profile.this, "Ошибка при удалении аккаунта", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(Profile.this, "Ошибка при удалении данных пользователя: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

}